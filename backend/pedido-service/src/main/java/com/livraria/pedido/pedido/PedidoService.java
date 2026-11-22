package com.livraria.pedido.pedido;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

import com.livraria.pedido.pedido.dto.NovaTentativaRequest;
import com.livraria.pedido.pedido.exceptions.PedidoNaoEncontradoException;
import com.livraria.pedido.pedido.exceptions.TentativaEmAndamentoException;
import org.springframework.dao.OptimisticLockingFailureException;
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
        return iniciarSaga(novo);
    }

    public Pedido buscar(String pedidoId, Long usuarioId){
        return  pedidoRepository.findByIdAndUsuarioId(pedidoId, usuarioId)
                .orElseThrow(PedidoNaoEncontradoException::new);
    }

    public Pedido tentarDeNovo(String pedidoId, NovaTentativaRequest request, Long usuarioId){
        Pedido pedido = buscar(pedidoId, usuarioId);
        pedido.novaTentativa(request.cartaoId(), novaTransacaoId());

        try{
            return iniciarSaga(pedido);
        }catch (OptimisticLockingFailureException ex){
            throw new TentativaEmAndamentoException();
        }
    }

    private Pedido iniciarSaga(Pedido pedido){
        return transacao.execute(status -> {
            Pedido salvo = pedidoRepository.save(pedido);
            Evento evento = eventoService.salvar(new Evento(salvo, LocalDateTime.now(clock)));
            sagaProducer.iniciar(evento);
            return salvo;
        });
    }

    private String novaTransacaoId() {
        return clock.millis() + "_" + UUID.randomUUID();
    }
}