package com.livraria.pedido.evento;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.livraria.pedido.evento.dto.FiltrosEvento;
import com.livraria.pedido.evento.exceptions.EventoNaoEncontradoException;
import com.livraria.pedido.evento.exceptions.FiltroNaoInformadoException;

@Service
public class EventoService {

    private static final Logger log = LoggerFactory.getLogger(EventoService.class);

    private final EventoRepository eventoRepository;
    private final Clock clock;

    public EventoService(EventoRepository eventoRepository, Clock clock) {
        this.eventoRepository = eventoRepository;
        this.clock = clock;
    }

    public Evento salvar(Evento evento) {
        return eventoRepository.save(evento);
    }

    public void notificarFim(Evento evento) {
        evento.registrarFim(LocalDateTime.now(clock));
        salvar(evento);
        log.info("Saga do pedido {} finalizada. Transacao: {}", evento.getPedidoId(), evento.getTransacaoId());
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
