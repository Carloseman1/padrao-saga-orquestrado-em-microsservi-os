package com.livraria.pedido.pedido;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

import com.livraria.pedido.pedido.dto.PedidoRequest;
import com.livraria.pedido.saga.OutboxRepository;

@SpringBootTest
@Testcontainers
class PedidoIntegracaoTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7");

    @Container
    @ServiceConnection
    static KafkaContainer kafka = new KafkaContainer("apache/kafka-native:3.8.1");

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private OutboxRepository outboxRepository;

    @Test
    void pedido_novo_inicia_a_saga_pela_outbox_com_o_pedido_como_chave() {
        try (Consumer<String, String> inicioSaga = consumidor("inicio-saga")) {
            Pedido pedido = pedidoService.criar(new PedidoRequest(
                    List.of(new ItemPedido(new Livro("DOM_CASMURRO", new BigDecimal("39.90")), 1)), 1L), 7L);

            List<ConsumerRecord<String, String>> recebidas = new ArrayList<>();
            await().atMost(Duration.ofSeconds(60)).until(() -> {
                inicioSaga.poll(Duration.ofMillis(500)).forEach(recebidas::add);
                return recebidas.stream().anyMatch(registro -> pedido.getId().equals(registro.key()));
            });

            await().atMost(Duration.ofSeconds(30)).until(() -> outboxRepository.findAll().stream()
                    .filter(mensagem -> mensagem.getChave().equals(pedido.getId()))
                    .allMatch(mensagem -> mensagem.getEnviadoEm() != null));
            assertThat(outboxRepository.findAll()).filteredOn(mensagem -> mensagem.getChave().equals(pedido.getId()))
                    .hasSize(1);
        }
    }

    private Consumer<String, String> consumidor(String topico) {
        Consumer<String, String> consumidor = new KafkaConsumer<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "teste-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class));
        consumidor.subscribe(List.of(topico));
        return consumidor;
    }
}
