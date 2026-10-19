package com.livraria.validacao.catalogo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.livraria.validacao.saga.exceptions.JsonInvalidoException;

@Component
public class LivroCadastradoProducer {

    private static final Logger log = LoggerFactory.getLogger(LivroCadastradoProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topicoLivroCadastrado;

    public LivroCadastradoProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper,
                                   @Value("${app.kafka.topico.livro-cadastrado}") String topicoLivroCadastrado) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topicoLivroCadastrado = topicoLivroCadastrado;
    }

    public void enviar(LivroCadastrado livro) {
        String payload = paraJson(livro);
        log.info("Enviando livro novo para o topico {}: {}", topicoLivroCadastrado, payload);
        kafkaTemplate.send(topicoLivroCadastrado, payload);
    }

    private String paraJson(LivroCadastrado livro) {
        try {
            return objectMapper.writeValueAsString(livro);
        } catch (JsonProcessingException ex) {
            throw new JsonInvalidoException(ex);
        }
    }
}
