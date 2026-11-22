package com.livraria.estoque.saga;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RelayOutbox {

    private static final Logger log = LoggerFactory.getLogger(RelayOutbox.class);

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final long timeoutEnvioMs;
    private final Clock clock;

    public RelayOutbox(OutboxRepository outboxRepository, KafkaTemplate<String, String> kafkaTemplate,
                       @Value("${app.outbox.timeout-envio-ms}") long timeoutEnvioMs, Clock clock) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.timeoutEnvioMs = timeoutEnvioMs;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${app.outbox.intervalo-ms}")
    public void publicarPendentes() {
        for (MensagemOutbox mensagem : outboxRepository.findTop100ByEnviadoEmIsNullOrderByIdAsc()) {
            try {
                kafkaTemplate.send(mensagem.getTopico(), mensagem.getChave(), mensagem.getPayload())
                        .get(timeoutEnvioMs, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            } catch (ExecutionException | TimeoutException ex) {
                log.warn("Falha ao publicar a mensagem {} da outbox, nova tentativa no próximo ciclo",
                        mensagem.getId(), ex);
                return;
            }

            mensagem.marcarEnviada(LocalDateTime.now(clock));
            outboxRepository.save(mensagem);
        }
    }
}
