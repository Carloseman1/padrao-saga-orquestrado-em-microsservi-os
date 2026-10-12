package com.livraria.pedido.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class KafkaConfig {

    @Bean
    public KafkaAdmin.NewTopics topicos(@Value("${app.kafka.topico.inicio-saga}") String inicioSaga,
                                        @Value("${app.kafka.topico.notificar-fim}") String notificarFim) {
        return new KafkaAdmin.NewTopics(
                TopicBuilder.name(inicioSaga).build(),
                TopicBuilder.name(notificarFim).build());
    }
}
