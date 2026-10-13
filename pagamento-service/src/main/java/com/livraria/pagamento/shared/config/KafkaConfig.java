package com.livraria.pagamento.shared.config;

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
    public KafkaAdmin.NewTopics topicos(@Value("${app.kafka.topico.orquestrador}") String orquestrador,
                                        @Value("${app.kafka.topico.pagamento-sucesso}") String sucesso,
                                        @Value("${app.kafka.topico.pagamento-falha}") String falha) {
        return new KafkaAdmin.NewTopics(topico(orquestrador), topico(sucesso), topico(falha));
    }

    private NewTopic topico(String nome) {
        return TopicBuilder.name(nome).partitions(PARTICOES).replicas(REPLICAS).build();
    }
}
