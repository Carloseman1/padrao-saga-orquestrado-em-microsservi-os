package com.livraria.estoque.estoque;

import java.time.Clock;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.livraria.estoque.estoque.exceptions.EstoqueNaoEncontradoException;
import com.livraria.estoque.estoque.exceptions.MovimentacaoDuplicadaException;
import com.livraria.estoque.saga.Evento;
import com.livraria.estoque.saga.ItemPedido;
import com.livraria.estoque.saga.OrquestradorProducer;
import com.livraria.estoque.saga.StatusSaga;
import com.livraria.estoque.shared.exception.IErroPadrao;

@Service
public class EstoqueService {

    private static final Logger log = LoggerFactory.getLogger(EstoqueService.class);
    private static final String ORIGEM = "ESTOQUE_SERVICE";

    private final EstoqueRepository estoqueRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;
    private final OrquestradorProducer orquestradorProducer;
    private final Clock clock;

    public EstoqueService(EstoqueRepository estoqueRepository, MovimentacaoEstoqueRepository movimentacaoRepository,
                          OrquestradorProducer orquestradorProducer, Clock clock) {
        this.estoqueRepository = estoqueRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.orquestradorProducer = orquestradorProducer;
        this.clock = clock;
    }

    public void baixar(Evento evento) {
        try {
            String pedidoId = evento.getPedido().getId();

            if (movimentacaoRepository.existsByPedidoIdAndTransacaoId(pedidoId, evento.getTransacaoId())) {
                throw new MovimentacaoDuplicadaException();
            }

            evento.getPedido().getItens().forEach(item -> baixarItem(item, pedidoId, evento.getTransacaoId()));
            evento.registrar(ORIGEM, StatusSaga.SUCESSO, "Estoque atualizado com sucesso.", agora());
        } catch (RuntimeException ex) {
            log.error("Erro ao atualizar o estoque", ex);
            evento.registrar(ORIGEM, StatusSaga.ROLLBACK_PENDENTE,
                    "Falha ao atualizar o estoque: " + motivo(ex), agora());
        }
        orquestradorProducer.enviar(evento);
    }

    public void desfazer(Evento evento) {
        try {
            movimentacaoRepository
                    .findByPedidoIdAndTransacaoId(evento.getPedido().getId(), evento.getTransacaoId())
                    .forEach(this::restaurar);
            evento.registrar(ORIGEM, StatusSaga.FALHA, "Rollback executado no estoque.", agora());
        } catch (RuntimeException ex) {
            log.error("Erro ao desfazer a baixa no estoque", ex);
            evento.registrar(ORIGEM, StatusSaga.FALHA,
                    "Rollback do estoque não executado: " + motivo(ex), agora());
        }
        orquestradorProducer.enviar(evento);
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

    private void restaurar(MovimentacaoEstoque movimentacao) {
        Estoque estoque = movimentacao.getEstoque();
        estoque.restaurar(movimentacao.getQuantidadeAnterior());
        estoqueRepository.save(estoque);
        log.info("Estoque do livro {} restaurado para {}", estoque.getCodigoLivro(), estoque.getDisponivel());
    }

    private String motivo(RuntimeException ex) {
        return ex instanceof IErroPadrao erro ? erro.toErrorDTO().message() : "erro inesperado.";
    }

    private LocalDateTime agora() {
        return LocalDateTime.now(clock);
    }
}
