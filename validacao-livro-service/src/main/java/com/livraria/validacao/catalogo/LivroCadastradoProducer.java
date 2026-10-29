package com.livraria.validacao.catalogo;

import java.time.Clock;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.livraria.validacao.saga.MensagemOutbox;
import com.livraria.validacao.saga.OutboxRepository;
import com.livraria.validacao.saga.exceptions.JsonInvalidoException;

@Component
public class LivroCadastradoProducer {

    private static final Logger log = LoggerFactory.getLogger(LivroCadastradoProducer.class);

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final String topicoLivroCadastrado;
    private final Clock clock;

    public LivroCadastradoProducer(OutboxRepository outboxRepository, ObjectMapper objectMapper,
                                   @Value("${app.kafka.topico.livro-cadastrado}") String topicoLivroCadastrado,
                                   Clock clock) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.topicoLivroCadastrado = topicoLivroCadastrado;
        this.clock = clock;
    }

    public void enviar(LivroCadastrado livro) {
        String payload = paraJson(livro);
        log.info("Registrando na outbox o livro novo para o topico {}: {}", topicoLivroCadastrado, payload);
        outboxRepository.save(new MensagemOutbox(
                topicoLivroCadastrado, livro.codigo(), payload, LocalDateTime.now(clock)));
    }

    private String paraJson(LivroCadastrado livro) {
        try {
            return objectMapper.writeValueAsString(livro);
        } catch (JsonProcessingException ex) {
            throw new JsonInvalidoException(ex);
        }
    }
}
