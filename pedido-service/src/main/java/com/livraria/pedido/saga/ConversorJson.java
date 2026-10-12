package com.livraria.pedido.saga;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.livraria.pedido.saga.exceptions.JsonInvalidoException;

@Component
public class ConversorJson {

    private final ObjectMapper objectMapper;

    public ConversorJson(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String paraJson(Object objeto) {
        try {
            return objectMapper.writeValueAsString(objeto);
        } catch (JsonProcessingException ex) {
            throw new JsonInvalidoException(ex);
        }
    }

    public <T> T paraObjeto(String json, Class<T> tipo) {
        try {
            return objectMapper.readValue(json, tipo);
        } catch (JsonProcessingException ex) {
            throw new JsonInvalidoException(ex);
        }
    }
}
