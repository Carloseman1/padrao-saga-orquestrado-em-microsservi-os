package com.livraria.pedido.evento;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.livraria.pedido.saga.ConversorJson;

@Component
public class EventoConsumer {

    private static final Logger log = LoggerFactory.getLogger(EventoConsumer.class);

    private final EventoService eventoService;
    private final ConversorJson conversorJson;

    public EventoConsumer(EventoService eventoService, ConversorJson conversorJson) {
        this.eventoService = eventoService;
        this.conversorJson = conversorJson;
    }

    @KafkaListener(topics = "${app.kafka.topico.notificar-fim}")
    public void notificarFim(String payload) {
        log.info("Recebendo notificacao de fim da saga: {}", payload);
        eventoService.notificarFim(conversorJson.paraEvento(payload));
    }
}
