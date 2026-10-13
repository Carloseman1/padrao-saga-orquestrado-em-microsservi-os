package com.livraria.orquestrador.shared.config;

import java.util.List;
import java.util.stream.Stream;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import com.livraria.orquestrador.orquestracao.Topico;
import com.livraria.orquestrador.orquestracao.exceptions.OrigemOuStatusNaoInformadoException;
import com.livraria.orquestrador.orquestracao.exceptions.TransicaoNaoEncontradaException;
import com.livraria.orquestrador.saga.exceptions.JsonInvalidoException;
import com.livraria.orquestrador.saga.exceptions.VersaoNaoSuportadaException;

@Configuration
public class KafkaConfig {

    private static final int PARTICOES = 1;
    private static final int REPLICAS = 1;
    private static final String SUFIXO_DLT = ".DLT";
    private static final long INTERVALO_ENTRE_TENTATIVAS_MS = 1000;
    private static final long NOVAS_TENTATIVAS = 2;

    private static final List<Topico> TOPICOS_ESCUTADOS =
            List.of(Topico.INICIO_SAGA, Topico.ORQUESTRADOR, Topico.FIM_SUCESSO, Topico.FIM_FALHA);

    @Bean
    public KafkaAdmin.NewTopics topicos() {
        Stream<NewTopic> todos = Stream.of(Topico.values())
                .map(topico -> topico(topico.getNome()));

        Stream<NewTopic> dlts = TOPICOS_ESCUTADOS.stream()
                .map(topico -> topico(topico.getNome() + SUFIXO_DLT));

        return new KafkaAdmin.NewTopics(Stream.concat(todos, dlts).toArray(NewTopic[]::new));
    }

    @Bean
    public DefaultErrorHandler tratadorDeFalhas(KafkaTemplate<String, String> kafkaTemplate) {
        DeadLetterPublishingRecoverer enviarParaDlt = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (mensagem, erro) -> new TopicPartition(mensagem.topic() + SUFIXO_DLT, mensagem.partition()));

        DefaultErrorHandler tratador = new DefaultErrorHandler(enviarParaDlt,
                new FixedBackOff(INTERVALO_ENTRE_TENTATIVAS_MS, NOVAS_TENTATIVAS));
        tratador.addNotRetryableExceptions(
                JsonInvalidoException.class,
                VersaoNaoSuportadaException.class,
                OrigemOuStatusNaoInformadoException.class,
                TransicaoNaoEncontradaException.class);
        return tratador;
    }

    private NewTopic topico(String nome) {
        return TopicBuilder.name(nome).partitions(PARTICOES).replicas(REPLICAS).build();
    }
}
