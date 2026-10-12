package com.livraria.estoque.saga;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrquestradorProducer {

    private static final Logger log = LoggerFactory.getLogger(OrquestradorProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ConversorJson conversorJson;
    private final String topicoOrquestrador;

    public OrquestradorProducer(KafkaTemplate<String, String> kafkaTemplate, ConversorJson conversorJson,
                                @Value("${app.kafka.topico.orquestrador}") String topicoOrquestrador) {
        this.kafkaTemplate = kafkaTemplate;
        this.conversorJson = conversorJson;
        this.topicoOrquestrador = topicoOrquestrador;
    }

    public void enviar(Evento evento) {
        String payload = conversorJson.paraJson(evento);
        log.info("Enviando evento para o topico {}: {}", topicoOrquestrador, payload);
        kafkaTemplate.send(topicoOrquestrador, payload);
    }
}
