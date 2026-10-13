package com.livraria.orquestrador.shared.config;

import java.util.Arrays;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import com.livraria.orquestrador.orquestracao.Topico;

@Configuration
public class KafkaConfig {

    private static final int PARTICOES = 1;
    private static final int REPLICAS = 1;

    @Bean
    public KafkaAdmin.NewTopics topicos() {
        return new KafkaAdmin.NewTopics(Arrays.stream(Topico.values())
                .map(topico -> TopicBuilder.name(topico.getNome())
                        .partitions(PARTICOES)
                        .replicas(REPLICAS)
                        .build())
                .toArray(NewTopic[]::new));
    }
}
