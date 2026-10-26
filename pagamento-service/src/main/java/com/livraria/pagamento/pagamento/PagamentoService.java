package com.livraria.pagamento.pagamento;

import java.time.Clock;
import java.time.LocalDateTime;

import com.livraria.pagamento.cartao.CartaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.livraria.pagamento.pagamento.exceptions.PagamentoDuplicadoException;
import com.livraria.pagamento.pagamento.exceptions.PagamentoNaoEncontradoException;
import com.livraria.pagamento.saga.Evento;
import com.livraria.pagamento.saga.OrquestradorProducer;
import com.livraria.pagamento.saga.Pedido;
import com.livraria.pagamento.saga.StatusSaga;
import com.livraria.pagamento.shared.exception.IErroPadrao;

@Service
public class PagamentoService {

    private static final Logger log = LoggerFactory.getLogger(PagamentoService.class);
    private static final String ORIGEM = "PAGAMENTO_SERVICE";

    private final PagamentoRepository pagamentoRepository;
    private final OrquestradorProducer orquestradorProducer;
    private final Clock clock;
    private final CartaoService cartaoService;

    public PagamentoService(PagamentoRepository pagamentoRepository, CartaoService cartaoService,
                            OrquestradorProducer orquestradorProducer, Clock clock) {
        this.pagamentoRepository = pagamentoRepository;
        this.cartaoService = cartaoService;
        this.orquestradorProducer = orquestradorProducer;
        this.clock = clock;
    }

    public void realizar(Evento evento) {
        try {
            cartaoService.exigirValidoParaPagamento(
                    evento.getPedido().getCartaoId(), evento.getPedido().getUsuarioId());

            Pagamento pagamento = criarPendente(evento);
            pagamento.aprovar(agora());
            pagamentoRepository.save(pagamento);
            evento.registrar(ORIGEM, StatusSaga.SUCESSO, "Pagamento realizado com sucesso.", agora());
        } catch (RuntimeException ex) {
            log.error("Erro ao realizar o pagamento", ex);
            evento.registrar(ORIGEM, StatusSaga.ROLLBACK_PENDENTE,
                    "Falha ao realizar o pagamento: " + motivo(ex), agora());
        }
        orquestradorProducer.enviar(evento);
    }

    public void estornar(Evento evento) {
        try {
            Pagamento pagamento = pagamentoRepository
                    .findByPedidoIdAndTransacaoId(evento.getPedido().getId(), evento.getTransacaoId())
                    .orElseThrow(PagamentoNaoEncontradoException::new);
            pagamento.estornar(agora());
            pagamentoRepository.save(pagamento);
            atualizarTotais(evento.getPedido(), pagamento);
            evento.registrar(ORIGEM, StatusSaga.FALHA, "Estorno do pagamento realizado.", agora());
        } catch (RuntimeException ex) {
            log.error("Erro ao estornar o pagamento", ex);
            evento.registrar(ORIGEM, StatusSaga.FALHA,
                    "Estorno do pagamento não realizado: " + motivo(ex), agora());
        }
        orquestradorProducer.enviar(evento);
    }

    private Pagamento criarPendente(Evento evento) {
        Pedido pedido = evento.getPedido();

        if (pagamentoRepository.existsByPedidoIdAndTransacaoId(pedido.getId(), evento.getTransacaoId())) {
            throw new PagamentoDuplicadoException();
        }

        Pagamento pagamento = pagamentoRepository.save(new Pagamento(
                pedido.getId(),
                evento.getTransacaoId(),
                pedido.calcularQuantidadeTotal(),
                pedido.calcularValorTotal(),
                agora()));

        atualizarTotais(pedido, pagamento);
        return pagamento;
    }

    private void atualizarTotais(Pedido pedido, Pagamento pagamento) {
        pedido.setValorTotal(pagamento.getValorTotal());
        pedido.setQuantidadeTotal(pagamento.getQuantidadeTotal());
    }

    private String motivo(RuntimeException ex) {
        return ex instanceof IErroPadrao erro ? erro.toErrorDTO().message() : "erro inesperado.";
    }

    private LocalDateTime agora() {
        return LocalDateTime.now(clock);
    }
}
