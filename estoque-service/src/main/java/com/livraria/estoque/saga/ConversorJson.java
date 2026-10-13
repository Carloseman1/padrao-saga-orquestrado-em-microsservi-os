package com.livraria.estoque.saga;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.livraria.estoque.saga.exceptions.JsonInvalidoException;
import com.livraria.estoque.saga.exceptions.VersaoNaoSuportadaException;

@Component
public class ConversorJson {

    private final ObjectMapper objectMapper;

    public ConversorJson(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String paraJson(Evento evento) {
        try {
            return objectMapper.writeValueAsString(evento);
        } catch (JsonProcessingException ex) {
            throw new JsonInvalidoException(ex);
        }
    }

    public Evento paraEvento(String json) {
        Evento evento = ler(json);

        if (!evento.versaoSuportada()) {
            throw new VersaoNaoSuportadaException(evento.getVersao());
        }

        return evento;
    }

    private Evento ler(String json) {
        try {
            return objectMapper.readValue(json, Evento.class);
        } catch (JsonProcessingException ex) {
            throw new JsonInvalidoException(ex);
        }
    }
}
