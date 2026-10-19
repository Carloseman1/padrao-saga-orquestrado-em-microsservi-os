package com.livraria.estoque.estoque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.livraria.estoque.saga.exceptions.JsonInvalidoException;

@Component
public class LivroCadastradoConsumer {

    private static final Logger log = LoggerFactory.getLogger(LivroCadastradoConsumer.class);

    private final EstoqueService estoqueService;
    private final ObjectMapper objectMapper;

    public LivroCadastradoConsumer(EstoqueService estoqueService, ObjectMapper objectMapper) {
        this.estoqueService = estoqueService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.kafka.topico.livro-cadastrado}")
    public void criarEstoque(String payload) {
        log.info("Recebendo livro novo do catálogo: {}", payload);
        estoqueService.criarParaLivroNovo(ler(payload));
    }

    private LivroCadastrado ler(String payload) {
        try {
            return objectMapper.readValue(payload, LivroCadastrado.class);
        } catch (JsonProcessingException ex) {
            throw new JsonInvalidoException(ex);
        }
    }
}
