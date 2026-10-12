package com.livraria.pedido.pedido;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.livraria.pedido.evento.Evento;
import com.livraria.pedido.evento.EventoService;
import com.livraria.pedido.pedido.dto.PedidoRequest;
import com.livraria.pedido.saga.SagaProducer;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final EventoService eventoService;
    private final SagaProducer sagaProducer;
    private final Clock clock;

    public PedidoService(PedidoRepository pedidoRepository, EventoService eventoService,
                         SagaProducer sagaProducer, Clock clock) {
        this.pedidoRepository = pedidoRepository;
        this.eventoService = eventoService;
        this.sagaProducer = sagaProducer;
        this.clock = clock;
    }

    public Pedido criar(PedidoRequest request) {
        Pedido pedido = pedidoRepository.save(
                new Pedido(request.itens(), novaTransacaoId(), LocalDateTime.now(clock)));

        Evento evento = eventoService.salvar(new Evento(pedido, LocalDateTime.now(clock)));
        sagaProducer.iniciar(evento);

        return pedido;
    }

    private String novaTransacaoId() {
        return clock.millis() + "_" + UUID.randomUUID();
    }
}
