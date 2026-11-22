package com.livraria.validacao.saga;

import java.time.Clock;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OrquestradorProducer {

    private static final Logger log = LoggerFactory.getLogger(OrquestradorProducer.class);

    private final OutboxRepository outboxRepository;
    private final ConversorJson conversorJson;
    private final String topicoOrquestrador;
    private final Clock clock;

    public OrquestradorProducer(OutboxRepository outboxRepository, ConversorJson conversorJson,
                                @Value("${app.kafka.topico.orquestrador}") String topicoOrquestrador, Clock clock) {
        this.outboxRepository = outboxRepository;
        this.conversorJson = conversorJson;
        this.topicoOrquestrador = topicoOrquestrador;
        this.clock = clock;
    }

    public void enviar(Evento evento) {
        String payload = conversorJson.paraJson(evento);
        log.info("Registrando na outbox a resposta para o topico {}: {}", topicoOrquestrador, payload);
        outboxRepository.save(new MensagemOutbox(
                topicoOrquestrador, evento.getPedido().getId(), payload, LocalDateTime.now(clock)));
    }
}
