package com.livraria.estoque.shared.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import com.livraria.estoque.saga.exceptions.JsonInvalidoException;
import com.livraria.estoque.saga.exceptions.VersaoNaoSuportadaException;

@Configuration
public class KafkaConfig {

    private static final int PARTICOES = 1;
    private static final int REPLICAS = 1;
    private static final String SUFIXO_DLT = ".DLT";
    private static final long INTERVALO_ENTRE_TENTATIVAS_MS = 1000;
    private static final long NOVAS_TENTATIVAS = 2;

    @Bean
    public KafkaAdmin.NewTopics topicos(@Value("${app.kafka.topico.orquestrador}") String orquestrador,
                                        @Value("${app.kafka.topico.estoque-sucesso}") String sucesso,
                                        @Value("${app.kafka.topico.estoque-falha}") String falha) {
        return new KafkaAdmin.NewTopics(
                topico(orquestrador),
                topico(sucesso), topico(sucesso + SUFIXO_DLT),
                topico(falha), topico(falha + SUFIXO_DLT));
    }

    @Bean
    public DefaultErrorHandler tratadorDeFalhas(KafkaTemplate<String, String> kafkaTemplate) {
        DeadLetterPublishingRecoverer enviarParaDlt = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (mensagem, erro) -> new TopicPartition(mensagem.topic() + SUFIXO_DLT, mensagem.partition()));

        DefaultErrorHandler tratador = new DefaultErrorHandler(enviarParaDlt,
                new FixedBackOff(INTERVALO_ENTRE_TENTATIVAS_MS, NOVAS_TENTATIVAS));
        tratador.addNotRetryableExceptions(JsonInvalidoException.class, VersaoNaoSuportadaException.class);
        return tratador;
    }

    private NewTopic topico(String nome) {
        return TopicBuilder.name(nome).partitions(PARTICOES).replicas(REPLICAS).build();
    }
}
