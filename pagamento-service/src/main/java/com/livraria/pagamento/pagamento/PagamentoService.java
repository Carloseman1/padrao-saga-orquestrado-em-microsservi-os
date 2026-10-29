package com.livraria.pagamento.pagamento;

import java.time.Clock;
import java.time.LocalDateTime;

import com.livraria.pagamento.cartao.CartaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

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
    private final TransactionTemplate transacao;
    private final Clock clock;
    private final CartaoService cartaoService;

    public PagamentoService(PagamentoRepository pagamentoRepository, CartaoService cartaoService,
                            OrquestradorProducer orquestradorProducer, TransactionTemplate transacao, Clock clock) {
        this.pagamentoRepository = pagamentoRepository;
        this.cartaoService = cartaoService;
        this.orquestradorProducer = orquestradorProducer;
        this.transacao = transacao;
        this.clock = clock;
    }

    public void realizar(Evento evento) {
        try {
            exigirNaoProcessado(evento);
            cartaoService.exigirValidoParaPagamento(
                    evento.getPedido().getCartaoId(), evento.getPedido().getUsuarioId());

            Pagamento pagamento = novoPagamento(evento);
            pagamento.aprovar(agora());
            evento.registrar(ORIGEM, StatusSaga.SUCESSO, "Pagamento realizado com sucesso.", agora());
            gravarComResposta(pagamento, evento);
        } catch (PagamentoDuplicadoException ex) {
            log.warn("Mensagem duplicada ignorada: o pagamento do pedido {} na transação {} já foi processado",
                    evento.getPedido().getId(), evento.getTransacaoId());
        } catch (RuntimeException ex) {
            log.error("Erro ao realizar o pagamento", ex);
            evento.registrar(ORIGEM, StatusSaga.ROLLBACK_PENDENTE,
                    "Falha ao realizar o pagamento: " + motivo(ex), agora());

            Pagamento recusado = novoPagamento(evento);
            recusado.recusar(agora());
            gravarComResposta(recusado, evento);
        }
    }

    public void estornar(Evento evento) {
        try {
            Pagamento pagamento = pagamentoRepository
                    .findByPedidoIdAndTransacaoId(evento.getPedido().getId(), evento.getTransacaoId())
                    .orElseThrow(PagamentoNaoEncontradoException::new);

            if (pagamento.foiRecusado()) {
                evento.registrar(ORIGEM, StatusSaga.FALHA, "Pagamento recusado, não há o que estornar.", agora());
                orquestradorProducer.enviar(evento);
                return;
            }

            pagamento.estornar(agora());
            atualizarTotais(evento.getPedido(), pagamento);
            evento.registrar(ORIGEM, StatusSaga.FALHA, "Estorno do pagamento realizado.", agora());
            gravarComResposta(pagamento, evento);
        } catch (RuntimeException ex) {
            log.error("Erro ao estornar o pagamento", ex);
            evento.registrar(ORIGEM, StatusSaga.FALHA,
                    "Estorno do pagamento não realizado: " + motivo(ex), agora());
            orquestradorProducer.enviar(evento);
        }
    }

    private void exigirNaoProcessado(Evento evento) {
        if (pagamentoRepository.existsByPedidoIdAndTransacaoId(evento.getPedido().getId(), evento.getTransacaoId())) {
            throw new PagamentoDuplicadoException();
        }
    }

    private Pagamento novoPagamento(Evento evento) {
        Pedido pedido = evento.getPedido();

        Pagamento pagamento = new Pagamento(
                pedido.getId(),
                evento.getTransacaoId(),
                pedido.calcularQuantidadeTotal(),
                pedido.calcularValorTotal(),
                agora());

        atualizarTotais(pedido, pagamento);
        return pagamento;
    }

    private void gravarComResposta(Pagamento pagamento, Evento evento) {
        try {
            transacao.executeWithoutResult(status -> {
                pagamentoRepository.save(pagamento);
                orquestradorProducer.enviar(evento);
            });
        } catch (DataIntegrityViolationException ex) {
            throw new PagamentoDuplicadoException();
        }
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
