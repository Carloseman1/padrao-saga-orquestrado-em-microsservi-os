package com.livraria.pedido.evento;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import com.livraria.pedido.pedido.Pedido;
import com.livraria.pedido.pedido.PedidoRepository;
import com.livraria.pedido.pedido.exceptions.PedidoNaoEncontradoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.livraria.pedido.evento.dto.FiltrosEvento;
import com.livraria.pedido.evento.exceptions.EventoNaoEncontradoException;
import com.livraria.pedido.evento.exceptions.FiltroNaoInformadoException;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class EventoService {

    private static final Logger log = LoggerFactory.getLogger(EventoService.class);

    private final EventoRepository eventoRepository;
    private final PedidoRepository pedidoRepository;
    private final TransactionTemplate transacao;
    private final Clock clock;

    public EventoService(EventoRepository eventoRepository, PedidoRepository pedidoRepository,
                         TransactionTemplate transacao, Clock clock) {
        this.eventoRepository = eventoRepository;
        this.pedidoRepository = pedidoRepository;
        this.transacao = transacao;
        this.clock = clock;
    }

    public Evento salvar(Evento evento) {
        return eventoRepository.save(evento);
    }

    public void notificarFim(Evento evento) {
        evento.registrarFim(agora());
        transacao.executeWithoutResult(status -> {
            salvar(evento);
            atualizarPedido(evento);
        });
        log.info("Saga do pedido {} finalizada. Transacao: {}", evento.getPedidoId(), evento.getTransacaoId());
    }

    private void atualizarPedido(Evento evento) {
        Pedido pedido = pedidoRepository.findById(evento.getPedidoId())
                .orElseThrow(PedidoNaoEncontradoException::new);

        if (pedido.finalizar(evento.getTransacaoId(), evento.resultado(),
                evento.motivo(), agora())) {
            pedidoRepository.save(pedido);
            return;
        }

        log.warn("Fim de saga ignorado no pedido {}: a transacao {} nao e a " +
                "tentativa atual ou ja foi finalizada", evento.getPedidoId(), evento.getTransacaoId());
    }

    private LocalDateTime agora() {
        return LocalDateTime.now(clock);
    }

    public List<Evento> listar(Long usuarioId) {
        return eventoRepository.findAllByPedidoUsuarioIdOrderByCriadoEmDesc(usuarioId);
    }

    public Evento buscar(FiltrosEvento filtros, Long usuarioId) {
        if (filtros.temPedidoId()) {
            return eventoRepository.findTop1ByPedidoIdAndPedidoUsuarioIdOrderByCriadoEmDesc(filtros.pedidoId(), usuarioId)
                    .orElseThrow(EventoNaoEncontradoException::new);
        }

        if (filtros.temTransacaoId()) {
            return eventoRepository.findTop1ByTransacaoIdAndPedidoUsuarioIdOrderByCriadoEmDesc(filtros.transacaoId(), usuarioId)
                    .orElseThrow(EventoNaoEncontradoException::new);
        }

        throw new FiltroNaoInformadoException();
    }
}
