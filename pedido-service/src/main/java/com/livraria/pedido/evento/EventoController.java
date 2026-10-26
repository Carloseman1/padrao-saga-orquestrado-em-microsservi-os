package com.livraria.pedido.evento;

import java.util.List;

import com.livraria.pedido.seguranca.UsuarioAutenticado;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.livraria.pedido.evento.dto.FiltrosEvento;

@RestController
@RequestMapping("/api/eventos")
public class EventoController {

    private final EventoService eventoService;
    private final UsuarioAutenticado usuarioAutenticado;

    public EventoController(EventoService eventoService, UsuarioAutenticado usuarioAutenticado) {
        this.eventoService = eventoService;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    @GetMapping
    public Evento buscar(FiltrosEvento filtros) {
        return eventoService.buscar(filtros, usuarioAutenticado.id());
    }

    @GetMapping("/todos")
    public List<Evento> listar() {
        return eventoService.listar(usuarioAutenticado.id());
    }
}
