package com.livraria.estoque.estoque;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.livraria.estoque.estoque.dto.EntradaEstoqueRequest;
import com.livraria.estoque.estoque.dto.EstoqueDTO;
import com.livraria.estoque.estoque.exceptions.EstoqueNaoEncontradoException;
import com.livraria.estoque.estoque.exceptions.MovimentacaoDuplicadaException;
import com.livraria.estoque.saga.Evento;
import com.livraria.estoque.saga.ItemPedido;
import com.livraria.estoque.saga.OrquestradorProducer;
import com.livraria.estoque.saga.StatusSaga;
import com.livraria.estoque.shared.exception.CampoObrigatorioException;
import com.livraria.estoque.shared.exception.IErroPadrao;

@Service
public class EstoqueService {

    private static final Logger log = LoggerFactory.getLogger(EstoqueService.class);
    private static final String ORIGEM = "ESTOQUE_SERVICE";

    private final EstoqueRepository estoqueRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;
    private final BaixaEstoqueRepository baixaRepository;
    private final OrquestradorProducer orquestradorProducer;
    private final TransactionTemplate transacao;
    private final Clock clock;

    public EstoqueService(EstoqueRepository estoqueRepository, MovimentacaoEstoqueRepository movimentacaoRepository,
                          BaixaEstoqueRepository baixaRepository, OrquestradorProducer orquestradorProducer,
                          TransactionTemplate transacao, Clock clock) {
        this.estoqueRepository = estoqueRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.baixaRepository = baixaRepository;
        this.orquestradorProducer = orquestradorProducer;
        this.transacao = transacao;
        this.clock = clock;
    }

    public void criarParaLivroNovo(LivroCadastrado livro) {
        if (estoqueRepository.findByCodigoLivro(livro.codigo()).isPresent()) {
            log.info("O livro {} já tem estoque, nada a criar", livro.codigo());
            return;
        }

        estoqueRepository.save(new Estoque(livro.codigo(), livro.quantidadeInicial()));
        log.info("Estoque criado para o livro {} com {} unidades", livro.codigo(), livro.quantidadeInicial());
    }

    public EstoqueDTO registrarEntrada(EntradaEstoqueRequest request) {
        if (request.codigoLivro() == null || request.codigoLivro().isBlank()) {
            throw new CampoObrigatorioException("código do livro");
        }

        String codigoLivro = request.codigoLivro().trim().toUpperCase();
        Estoque estoque = estoqueRepository.findByCodigoLivro(codigoLivro)
                .orElseGet(() -> new Estoque(codigoLivro, 0));

        estoque.repor(request.quantidade());
        return EstoqueDTO.de(estoqueRepository.save(estoque));
    }

    public List<EstoqueDTO> listar() {
        return estoqueRepository.findAll(Sort.by("codigoLivro")).stream()
                .map(EstoqueDTO::de)
                .toList();
    }

    public void baixar(Evento evento) {
        try {
            String pedidoId = evento.getPedido().getId();

            if (baixaRepository.existsByPedidoIdAndTransacaoId(pedidoId, evento.getTransacaoId())) {
                throw new MovimentacaoDuplicadaException();
            }

            transacao.executeWithoutResult(status -> {
                salvarBaixa(new BaixaEstoque(pedidoId, evento.getTransacaoId(), true, agora()));
                evento.getPedido().getItens().forEach(item -> baixarItem(item, pedidoId, evento.getTransacaoId()));
                evento.registrar(ORIGEM, StatusSaga.SUCESSO, "Estoque atualizado com sucesso.", agora());
                orquestradorProducer.enviar(evento);
            });
        } catch (MovimentacaoDuplicadaException ex) {
            log.warn("Mensagem duplicada ignorada: o estoque do pedido {} na transação {} já foi baixado",
                    evento.getPedido().getId(), evento.getTransacaoId());
        } catch (OptimisticLockingFailureException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.error("Erro ao atualizar o estoque", ex);
            evento.registrar(ORIGEM, StatusSaga.ROLLBACK_PENDENTE,
                    "Falha ao atualizar o estoque: " + motivo(ex), agora());
            transacao.executeWithoutResult(status -> {
                salvarBaixa(new BaixaEstoque(evento.getPedido().getId(), evento.getTransacaoId(), false, agora()));
                orquestradorProducer.enviar(evento);
            });
        }
    }

    public void desfazer(Evento evento) {
        try {
            transacao.executeWithoutResult(status -> {
                movimentacaoRepository
                        .findByPedidoIdAndTransacaoId(evento.getPedido().getId(), evento.getTransacaoId())
                        .stream()
                        .filter(movimentacao -> !movimentacao.isEstornada())
                        .forEach(this::devolver);
                evento.registrar(ORIGEM, StatusSaga.FALHA, "Rollback executado no estoque.", agora());
                orquestradorProducer.enviar(evento);
            });
        } catch (OptimisticLockingFailureException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.error("Erro ao desfazer a baixa no estoque", ex);
            evento.registrar(ORIGEM, StatusSaga.FALHA,
                    "Rollback do estoque não executado: " + motivo(ex), agora());
            orquestradorProducer.enviar(evento);
        }
    }

    private void salvarBaixa(BaixaEstoque baixa) {
        try {
            baixaRepository.save(baixa);
        } catch (DataIntegrityViolationException ex) {
            throw new MovimentacaoDuplicadaException();
        }
    }

    private void baixarItem(ItemPedido item, String pedidoId, String transacaoId) {
        Estoque estoque = estoqueRepository.findByCodigoLivro(item.livro().codigo())
                .orElseThrow(EstoqueNaoEncontradoException::new);

        MovimentacaoEstoque movimentacao =
                new MovimentacaoEstoque(estoque, pedidoId, transacaoId, item.quantidade(), agora());

        estoque.baixar(item.quantidade());
        estoqueRepository.save(estoque);
        movimentacaoRepository.save(movimentacao);
    }

    private void devolver(MovimentacaoEstoque movimentacao) {
        Estoque estoque = movimentacao.getEstoque();
        estoque.repor(movimentacao.getQuantidadePedida());
        movimentacao.estornar(agora());
        estoqueRepository.save(estoque);
        movimentacaoRepository.save(movimentacao);
        log.info("Devolvidas {} unidades do livro {}, estoque agora com {}",
                movimentacao.getQuantidadePedida(), estoque.getCodigoLivro(),
                estoque.getDisponivel());
    }

    private String motivo(RuntimeException ex) {
        return ex instanceof IErroPadrao erro ? erro.toErrorDTO().message() : "erro inesperado.";
    }

    private LocalDateTime agora() {
        return LocalDateTime.now(clock);
    }
}
