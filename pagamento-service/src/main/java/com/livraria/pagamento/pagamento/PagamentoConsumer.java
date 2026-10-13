package com.livraria.pagamento.pagamento;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.livraria.pagamento.saga.ConversorJson;

@Component
public class PagamentoConsumer {

    private static final Logger log = LoggerFactory.getLogger(PagamentoConsumer.class);

    private final PagamentoService pagamentoService;
    private final ConversorJson conversorJson;

    public PagamentoConsumer(PagamentoService pagamentoService, ConversorJson conversorJson) {
        this.pagamentoService = pagamentoService;
        this.conversorJson = conversorJson;
    }

    @KafkaListener(topics = "${app.kafka.topico.pagamento-sucesso}")
    public void realizar(String payload) {
        log.info("Recebendo evento para realizar pagamento: {}", payload);
        pagamentoService.realizar(conversorJson.paraEvento(payload));
    }

    @KafkaListener(topics = "${app.kafka.topico.pagamento-falha}")
    public void estornar(String payload) {
        log.info("Recebendo evento de estorno do pagamento: {}", payload);
        pagamentoService.estornar(conversorJson.paraEvento(payload));
    }
}
