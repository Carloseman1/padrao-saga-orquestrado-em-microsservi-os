package com.livraria.pedido.evento;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.livraria.pedido.evento.dto.FiltrosEvento;

@RestController
@RequestMapping("/api/eventos")
public class EventoController {

    private final EventoService eventoService;

    public EventoController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    @GetMapping
    public Evento buscar(FiltrosEvento filtros) {
        return eventoService.buscar(filtros);
    }

    @GetMapping("/todos")
    public List<Evento> listar() {
        return eventoService.listar();
    }
}
