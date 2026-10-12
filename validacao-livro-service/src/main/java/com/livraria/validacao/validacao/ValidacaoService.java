package com.livraria.validacao.validacao;

import java.time.Clock;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.livraria.validacao.catalogo.LivroRepository;
import com.livraria.validacao.saga.Evento;
import com.livraria.validacao.saga.Livro;
import com.livraria.validacao.saga.OrquestradorProducer;
import com.livraria.validacao.saga.Pedido;
import com.livraria.validacao.saga.StatusSaga;
import com.livraria.validacao.shared.exception.IErroPadrao;
import com.livraria.validacao.validacao.exceptions.LivroNaoCadastradoException;
import com.livraria.validacao.validacao.exceptions.LivroNaoInformadoException;
import com.livraria.validacao.validacao.exceptions.PedidoNaoIdentificadoException;
import com.livraria.validacao.validacao.exceptions.PedidoSemItensException;
import com.livraria.validacao.validacao.exceptions.ValidacaoDuplicadaException;

@Service
public class ValidacaoService {

    private static final Logger log = LoggerFactory.getLogger(ValidacaoService.class);
    private static final String ORIGEM = "VALIDACAO_LIVRO_SERVICE";

    private final LivroRepository livroRepository;
    private final ValidacaoRepository validacaoRepository;
    private final OrquestradorProducer orquestradorProducer;
    private final Clock clock;

    public ValidacaoService(LivroRepository livroRepository, ValidacaoRepository validacaoRepository,
                            OrquestradorProducer orquestradorProducer, Clock clock) {
        this.livroRepository = livroRepository;
        this.validacaoRepository = validacaoRepository;
        this.orquestradorProducer = orquestradorProducer;
        this.clock = clock;
    }

    public void validar(Evento evento) {
        try {
            validarPedido(evento);
            validacaoRepository.save(
                    new Validacao(evento.getPedido().getId(), evento.getTransacaoId(), true, agora()));
            evento.registrar(ORIGEM, StatusSaga.SUCESSO, "Livros validados com sucesso.", agora());
        } catch (RuntimeException ex) {
            log.error("Erro ao validar os livros do pedido", ex);
            evento.registrar(ORIGEM, StatusSaga.ROLLBACK_PENDENTE,
                    "Falha ao validar os livros: " + motivo(ex), agora());
        }
        orquestradorProducer.enviar(evento);
    }

    public void desfazer(Evento evento) {
        String pedidoId = evento.getPedido().getId();
        Validacao validacao = validacaoRepository
                .findByPedidoIdAndTransacaoId(pedidoId, evento.getTransacaoId())
                .orElseGet(() -> new Validacao(pedidoId, evento.getTransacaoId(), false, agora()));

        validacao.marcarFalha(agora());
        validacaoRepository.save(validacao);

        evento.registrar(ORIGEM, StatusSaga.FALHA, "Rollback executado na validação dos livros.", agora());
        orquestradorProducer.enviar(evento);
    }

    private void validarPedido(Evento evento) {
        Pedido pedido = evento.getPedido();

        if (pedido == null || !pedido.temItens()) {
            throw new PedidoSemItensException();
        }

        if (vazio(pedido.getId()) || vazio(evento.getTransacaoId())) {
            throw new PedidoNaoIdentificadoException();
        }

        if (validacaoRepository.existsByPedidoIdAndTransacaoId(pedido.getId(), evento.getTransacaoId())) {
            throw new ValidacaoDuplicadaException();
        }

        pedido.getItens().forEach(item -> validarLivro(item.livro()));
    }

    private void validarLivro(Livro livro) {
        if (livro == null || vazio(livro.codigo())) {
            throw new LivroNaoInformadoException();
        }

        if (!livroRepository.existsByCodigo(livro.codigo())) {
            throw new LivroNaoCadastradoException();
        }
    }

    private boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }

    private String motivo(RuntimeException ex) {
        return ex instanceof IErroPadrao erro ? erro.toErrorDTO().message() : "erro inesperado.";
    }

    private LocalDateTime agora() {
        return LocalDateTime.now(clock);
    }
}
