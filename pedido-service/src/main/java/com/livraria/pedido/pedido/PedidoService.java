package com.livraria.pedido.pedido;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.livraria.pedido.evento.Evento;
import com.livraria.pedido.evento.EventoService;
import com.livraria.pedido.pedido.dto.PedidoRequest;
import com.livraria.pedido.saga.SagaProducer;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final EventoService eventoService;
    private final SagaProducer sagaProducer;
    private final TransactionTemplate transacao;
    private final Clock clock;

    public PedidoService(PedidoRepository pedidoRepository, EventoService eventoService,
                         SagaProducer sagaProducer, TransactionTemplate transacao, Clock clock) {
        this.pedidoRepository = pedidoRepository;
        this.eventoService = eventoService;
        this.sagaProducer = sagaProducer;
        this.transacao = transacao;
        this.clock = clock;
    }

    public Pedido criar(PedidoRequest request, Long usuarioId) {
        Pedido novo = new Pedido(request.itens(), usuarioId, request.cartaoId(), novaTransacaoId(),
                LocalDateTime.now(clock));

        return transacao.execute(status -> {
            Pedido pedido = pedidoRepository.save(novo);
            Evento evento = eventoService.salvar(new Evento(pedido, LocalDateTime.now(clock)));
            sagaProducer.iniciar(evento);
            return pedido;
        });
    }

    private String novaTransacaoId() {
        return clock.millis() + "_" + UUID.randomUUID();
    }
}