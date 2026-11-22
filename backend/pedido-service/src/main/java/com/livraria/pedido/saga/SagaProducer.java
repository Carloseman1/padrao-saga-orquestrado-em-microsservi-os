package com.livraria.pedido.saga;

import java.time.Clock;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.livraria.pedido.evento.Evento;

@Component
public class SagaProducer {

    private static final Logger log = LoggerFactory.getLogger(SagaProducer.class);

    private final OutboxRepository outboxRepository;
    private final ConversorJson conversorJson;
    private final String topicoInicioSaga;
    private final Clock clock;

    public SagaProducer(OutboxRepository outboxRepository, ConversorJson conversorJson,
                        @Value("${app.kafka.topico.inicio-saga}") String topicoInicioSaga, Clock clock) {
        this.outboxRepository = outboxRepository;
        this.conversorJson = conversorJson;
        this.topicoInicioSaga = topicoInicioSaga;
        this.clock = clock;
    }

    public void iniciar(Evento evento) {
        String payload = conversorJson.paraJson(evento);
        log.info("Registrando na outbox o inicio da saga para o topico {}: {}", topicoInicioSaga, payload);
        outboxRepository.save(new MensagemOutbox(
                topicoInicioSaga, evento.getPedidoId(), payload, LocalDateTime.now(clock)));
    }
}
