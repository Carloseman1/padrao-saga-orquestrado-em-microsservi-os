package com.livraria.pagamento.pagamento;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

import com.livraria.pagamento.cartao.CartaoService;
import com.livraria.pagamento.cartao.exceptions.CartaoVencidoException;
import com.livraria.pagamento.saga.ConversorJson;
import com.livraria.pagamento.saga.Evento;
import com.livraria.pagamento.saga.ItemPedido;
import com.livraria.pagamento.saga.Livro;
import com.livraria.pagamento.saga.OutboxRepository;
import com.livraria.pagamento.saga.Pedido;
import com.livraria.pagamento.saga.StatusSaga;

@SpringBootTest(properties = "spring.kafka.consumer.auto-offset-reset=earliest")
@Testcontainers
class PagamentoIntegracaoTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    @ServiceConnection
    static KafkaContainer kafka = new KafkaContainer("apache/kafka-native:3.8.1");

    @MockitoBean
    private CartaoService cartaoService;

    @Autowired
    private PagamentoService pagamentoService;

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ConversorJson conversorJson;

    @Test
    void mensagem_duplicada_no_kafka_cobra_e_responde_uma_vez_so() throws Exception {
        String duplicada = conversorJson.paraJson(evento("pedido-duplicado"));
        String marcador = conversorJson.paraJson(evento("pedido-marcador"));

        try (Consumer<String, String> orquestrador = consumidorDoOrquestrador()) {
            kafkaTemplate.send("pagamento-sucesso", duplicada).get();
            kafkaTemplate.send("pagamento-sucesso", duplicada).get();
            kafkaTemplate.send("pagamento-sucesso", marcador).get();

            List<String> respostas = respostasAte(orquestrador, "pedido-marcador");

            assertThat(respostas).filteredOn("pedido-duplicado"::equals).hasSize(1);
        }

        assertThat(pagamentosDo("pedido-duplicado")).isEqualTo(1);
    }

    @Test
    void banco_recusa_a_copia_que_passou_pela_conferencia_junto_com_a_primeira() {
        doAnswer(invocacao -> pagamentoRepository.save(new Pagamento("pedido-corrida", "transacao-pedido-corrida",
                1, new BigDecimal("30.00"), LocalDateTime.now())))
                .when(cartaoService).exigirValidoParaPagamento(any(), any());
        Evento evento = evento("pedido-corrida");

        pagamentoService.realizar(evento);

        assertThat(pagamentosDo("pedido-corrida")).isEqualTo(1);
        assertThat(respostasNaOutbox("pedido-corrida")).isZero();
    }

    @Test
    void copia_atrasada_de_um_pagamento_recusado_nao_cobra_depois() {
        doThrow(new CartaoVencidoException()).when(cartaoService).exigirValidoParaPagamento(any(), any());
        Evento primeira = evento("pedido-recusado");
        pagamentoService.realizar(primeira);

        doNothing().when(cartaoService).exigirValidoParaPagamento(any(), any());
        Evento atrasada = evento("pedido-recusado");
        pagamentoService.realizar(atrasada);

        assertThat(primeira.getStatus()).isEqualTo(StatusSaga.ROLLBACK_PENDENTE);
        assertThat(atrasada.getStatus()).isNull();
        assertThat(pagamentoRepository.findByPedidoIdAndTransacaoId("pedido-recusado", "transacao-pedido-recusado"))
                .get()
                .extracting(Pagamento::getStatus)
                .isEqualTo(StatusPagamento.RECUSADO);
    }

    @Test
    void pagamento_aprovado_e_gravado_uma_vez_ja_com_o_status_final() {
        Evento evento = evento("pedido-aprovado");

        pagamentoService.realizar(evento);

        assertThat(evento.getStatus()).isEqualTo(StatusSaga.SUCESSO);
        assertThat(pagamentoRepository.findByPedidoIdAndTransacaoId("pedido-aprovado", "transacao-pedido-aprovado"))
                .get()
                .extracting(Pagamento::getStatus)
                .isEqualTo(StatusPagamento.APROVADO);
    }

    @Test
    void resposta_gravada_na_outbox_e_marcada_como_enviada_depois_de_publicada() {
        Evento evento = evento("pedido-outbox");

        pagamentoService.realizar(evento);

        assertThat(respostasNaOutbox("pedido-outbox")).isEqualTo(1);
        await().atMost(Duration.ofSeconds(30)).until(() -> outboxRepository.findAll().stream()
                .filter(mensagem -> mensagem.getChave().equals("pedido-outbox"))
                .allMatch(mensagem -> mensagem.getEnviadoEm() != null));
    }

    private long respostasNaOutbox(String pedidoId) {
        return outboxRepository.findAll().stream()
                .filter(mensagem -> mensagem.getChave().equals(pedidoId))
                .count();
    }

    private long pagamentosDo(String pedidoId) {
        return pagamentoRepository.findAll().stream()
                .filter(pagamento -> pagamento.getPedidoId().equals(pedidoId))
                .count();
    }

    private List<String> respostasAte(Consumer<String, String> orquestrador, String ultimoPedido) {
        List<String> pedidos = new ArrayList<>();
        await().atMost(Duration.ofSeconds(60)).until(() -> {
            orquestrador.poll(Duration.ofMillis(500))
                    .forEach(registro -> pedidos.add(conversorJson.paraEvento(registro.value()).getPedido().getId()));
            return pedidos.contains(ultimoPedido);
        });
        return pedidos;
    }

    private Consumer<String, String> consumidorDoOrquestrador() {
        Consumer<String, String> consumidor = new KafkaConsumer<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "teste-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class));
        consumidor.subscribe(List.of("orquestrador"));
        return consumidor;
    }

    private Evento evento(String pedidoId) {
        Pedido pedido = new Pedido();
        pedido.setId(pedidoId);
        pedido.setItens(List.of(new ItemPedido(new Livro("DOM_CASMURRO", new BigDecimal("30.00")), 1)));
        pedido.setUsuarioId(7L);
        pedido.setCartaoId(1L);

        Evento evento = new Evento();
        evento.setVersao(Evento.VERSAO_ATUAL);
        evento.setPedidoId(pedidoId);
        evento.setTransacaoId("transacao-" + pedidoId);
        evento.setPedido(pedido);
        return evento;
    }
}
