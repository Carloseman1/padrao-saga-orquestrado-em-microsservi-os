package com.livraria.orquestrador.orquestracao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.livraria.orquestrador.saga.ConversorJson;

@Component
public class OrquestracaoConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrquestracaoConsumer.class);

    private final OrquestracaoService orquestracaoService;
    private final ConversorJson conversorJson;

    public OrquestracaoConsumer(OrquestracaoService orquestracaoService, ConversorJson conversorJson) {
        this.orquestracaoService = orquestracaoService;
        this.conversorJson = conversorJson;
    }

    @KafkaListener(topics = "${app.kafka.topico.inicio-saga}")
    public void iniciar(String payload) {
        log.info("Recebendo evento de inicio da saga: {}", payload);
        orquestracaoService.iniciar(conversorJson.paraEvento(payload));
    }

    @KafkaListener(topics = "${app.kafka.topico.orquestrador}")
    public void continuar(String payload) {
        log.info("Recebendo evento para continuar a saga: {}", payload);
        orquestracaoService.continuar(conversorJson.paraEvento(payload));
    }

    @KafkaListener(topics = "${app.kafka.topico.fim-sucesso}")
    public void finalizarComSucesso(String payload) {
        log.info("Recebendo evento de fim da saga com sucesso: {}", payload);
        orquestracaoService.finalizarComSucesso(conversorJson.paraEvento(payload));
    }

    @KafkaListener(topics = "${app.kafka.topico.fim-falha}")
    public void finalizarComFalha(String payload) {
        log.info("Recebendo evento de fim da saga com falha: {}", payload);
        orquestracaoService.finalizarComFalha(conversorJson.paraEvento(payload));
    }
}
