package com.livraria.orquestrador.saga;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.livraria.orquestrador.orquestracao.Topico;

@Component
public class SagaProducer {

    private static final Logger log = LoggerFactory.getLogger(SagaProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ConversorJson conversorJson;

    public SagaProducer(KafkaTemplate<String, String> kafkaTemplate, ConversorJson conversorJson) {
        this.kafkaTemplate = kafkaTemplate;
        this.conversorJson = conversorJson;
    }

    public void enviar(Evento evento, Topico topico) {
        String payload = conversorJson.paraJson(evento);
        log.info("Enviando evento para o topico {}: {}", topico.getNome(), payload);
        kafkaTemplate.send(topico.getNome(), payload);
    }
}
