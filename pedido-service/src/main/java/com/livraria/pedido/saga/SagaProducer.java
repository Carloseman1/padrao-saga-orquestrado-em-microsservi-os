package com.livraria.pedido.saga;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.livraria.pedido.evento.Evento;

@Component
public class SagaProducer {

    private static final Logger log = LoggerFactory.getLogger(SagaProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ConversorJson conversorJson;
    private final String topicoInicioSaga;

    public SagaProducer(KafkaTemplate<String, String> kafkaTemplate, ConversorJson conversorJson,
                        @Value("${app.kafka.topico.inicio-saga}") String topicoInicioSaga) {
        this.kafkaTemplate = kafkaTemplate;
        this.conversorJson = conversorJson;
        this.topicoInicioSaga = topicoInicioSaga;
    }

    public void iniciar(Evento evento) {
        String payload = conversorJson.paraJson(evento);
        log.info("Enviando evento para o topico {}: {}", topicoInicioSaga, payload);
        kafkaTemplate.send(topicoInicioSaga, payload);
    }
}
