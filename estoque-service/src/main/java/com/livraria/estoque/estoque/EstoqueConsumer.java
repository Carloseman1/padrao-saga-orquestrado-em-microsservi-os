package com.livraria.estoque.estoque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.livraria.estoque.saga.ConversorJson;
import com.livraria.estoque.saga.Evento;

@Component
public class EstoqueConsumer {

    private static final Logger log = LoggerFactory.getLogger(EstoqueConsumer.class);

    private final EstoqueService estoqueService;
    private final ConversorJson conversorJson;

    public EstoqueConsumer(EstoqueService estoqueService, ConversorJson conversorJson) {
        this.estoqueService = estoqueService;
        this.conversorJson = conversorJson;
    }

    @KafkaListener(topics = "${app.kafka.topico.estoque-sucesso}")
    public void baixar(String payload) {
        log.info("Recebendo evento para baixar estoque: {}", payload);
        estoqueService.baixar(conversorJson.paraObjeto(payload, Evento.class));
    }

    @KafkaListener(topics = "${app.kafka.topico.estoque-falha}")
    public void desfazer(String payload) {
        log.info("Recebendo evento de rollback do estoque: {}", payload);
        estoqueService.desfazer(conversorJson.paraObjeto(payload, Evento.class));
    }
}
