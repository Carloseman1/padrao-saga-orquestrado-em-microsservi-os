package com.livraria.pedido.shared.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class KafkaConfig {

    private static final int PARTICOES = 1;
    private static final int REPLICAS = 1;

    @Bean
    public KafkaAdmin.NewTopics topicos(@Value("${app.kafka.topico.inicio-saga}") String inicioSaga,
                                        @Value("${app.kafka.topico.notificar-fim}") String notificarFim) {
        return new KafkaAdmin.NewTopics(topico(inicioSaga), topico(notificarFim));
    }

    private NewTopic topico(String nome) {
        return TopicBuilder.name(nome).partitions(PARTICOES).replicas(REPLICAS).build();
    }
}
