package com.livraria.validacao.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class KafkaConfig {

    @Bean
    public KafkaAdmin.NewTopics topicos(@Value("${app.kafka.topico.orquestrador}") String orquestrador,
                                        @Value("${app.kafka.topico.validacao-livro-sucesso}") String sucesso,
                                        @Value("${app.kafka.topico.validacao-livro-falha}") String falha) {
        return new KafkaAdmin.NewTopics(
                TopicBuilder.name(orquestrador).build(),
                TopicBuilder.name(sucesso).build(),
                TopicBuilder.name(falha).build());
    }
}
