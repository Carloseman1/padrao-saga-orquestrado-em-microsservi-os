package com.livraria.fiscal.fiscal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.livraria.fiscal.saga.ConversorJson;

@Component
public class FiscalConsumer {

    private static final Logger log = LoggerFactory.getLogger(FiscalConsumer.class);

    private final FiscalService fiscalService;
    private final ConversorJson conversorJson;

    public FiscalConsumer(FiscalService fiscalService, ConversorJson conversorJson) {
        this.fiscalService = fiscalService;
        this.conversorJson = conversorJson;
    }

    @KafkaListener(topics = "${app.kafka.topico.nfe-sucesso}")
    public void emitir(String payload) {
        log.info("Recebendo evento para emitir a NF-e: {}", payload);
        fiscalService.emitir(conversorJson.paraEvento(payload));
    }

    @KafkaListener(topics = "${app.kafka.topico.nfe-falha}")
    public void cancelar(String payload) {
        log.info("Recebendo evento para cancelar a NF-e: {}", payload);
        fiscalService.cancelar(conversorJson.paraEvento(payload));
    }
}
