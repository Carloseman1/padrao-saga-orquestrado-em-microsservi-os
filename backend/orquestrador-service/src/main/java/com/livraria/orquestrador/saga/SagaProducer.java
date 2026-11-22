package com.livraria.orquestrador.saga;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.livraria.orquestrador.orquestracao.Topico;
import com.livraria.orquestrador.saga.exceptions.EnvioNaoConfirmadoException;

@Component
public class SagaProducer {

    private static final Logger log = LoggerFactory.getLogger(SagaProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ConversorJson conversorJson;
    private final long timeoutEnvioMs;

    public SagaProducer(KafkaTemplate<String, String> kafkaTemplate, ConversorJson conversorJson,
                        @Value("${app.kafka.timeout-envio-ms}") long timeoutEnvioMs) {
        this.kafkaTemplate = kafkaTemplate;
        this.conversorJson = conversorJson;
        this.timeoutEnvioMs = timeoutEnvioMs;
    }

    public void enviar(Evento evento, Topico topico) {
        String payload = conversorJson.paraJson(evento);
        log.info("Enviando evento para o topico {}: {}", topico.getNome(), payload);
        try {
            kafkaTemplate.send(topico.getNome(), evento.getPedido().getId(), payload)
                    .get(timeoutEnvioMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new EnvioNaoConfirmadoException(ex);
        } catch (ExecutionException | TimeoutException ex) {
            throw new EnvioNaoConfirmadoException(ex);
        }
    }
}
