package com.livraria.estoque.estoque;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import java.math.BigDecimal;
import java.time.Duration;
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
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

import com.livraria.estoque.saga.ConversorJson;
import com.livraria.estoque.saga.Evento;
import com.livraria.estoque.saga.ItemPedido;
import com.livraria.estoque.saga.Livro;
import com.livraria.estoque.saga.Pedido;
import com.livraria.estoque.saga.StatusSaga;

@SpringBootTest(properties = "spring.kafka.consumer.auto-offset-reset=earliest")
@Testcontainers
class EstoqueIntegracaoTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    @ServiceConnection
    static KafkaContainer kafka = new KafkaContainer("apache/kafka-native:3.8.1");

    @Autowired
    private EstoqueService estoqueService;

    @Autowired
    private EstoqueRepository estoqueRepository;

    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ConversorJson conversorJson;

    @Test
    void mensagem_duplicada_no_kafka_baixa_o_estoque_e_responde_uma_vez_so() throws Exception {
        estoqueRepository.save(new Estoque("DUPLICADO", 5));
        estoqueRepository.save(new Estoque("MARCADOR", 5));
        String duplicada = conversorJson.paraJson(evento("pedido-duplicado", item("DUPLICADO", 2)));
        String marcador = conversorJson.paraJson(evento("pedido-marcador", item("MARCADOR", 1)));

        try (Consumer<String, String> orquestrador = consumidorDoOrquestrador()) {
            kafkaTemplate.send("estoque-sucesso", duplicada).get();
            kafkaTemplate.send("estoque-sucesso", duplicada).get();
            kafkaTemplate.send("estoque-sucesso", marcador).get();

            List<String> respostas = respostasAte(orquestrador, "pedido-marcador");

            assertThat(respostas).filteredOn("pedido-duplicado"::equals).hasSize(1);
        }

        assertThat(disponivel("DUPLICADO")).isEqualTo(3);
        assertThat(movimentacaoRepository
                .findByPedidoIdAndTransacaoId("pedido-duplicado", "transacao-pedido-duplicado")).hasSize(1);
    }

    @Test
    void rollback_devolve_so_o_que_o_pedido_baixou_sem_apagar_a_venda_de_outro() {
        estoqueRepository.save(new Estoque("CRUZADO", 2));
        Evento pedidoA = evento("pedido-a", item("CRUZADO", 1));
        Evento pedidoB = evento("pedido-b", item("CRUZADO", 1));

        estoqueService.baixar(pedidoA);
        estoqueService.baixar(pedidoB);
        estoqueService.desfazer(pedidoA);

        assertThat(disponivel("CRUZADO")).isEqualTo(1);
    }

    @Test
    void pedido_com_um_item_sem_estoque_nao_baixa_nenhum_item() {
        estoqueRepository.save(new Estoque("ATOMICO_A", 5));
        estoqueRepository.save(new Estoque("ATOMICO_B", 1));
        Evento evento = evento("pedido-atomico", item("ATOMICO_A", 2), item("ATOMICO_B", 3));

        estoqueService.baixar(evento);

        assertThat(evento.getStatus()).isEqualTo(StatusSaga.ROLLBACK_PENDENTE);
        assertThat(disponivel("ATOMICO_A")).isEqualTo(5);
        assertThat(movimentacaoRepository
                .findByPedidoIdAndTransacaoId("pedido-atomico", "transacao-pedido-atomico")).isEmpty();
    }

    @Test
    void copia_atrasada_de_uma_baixa_recusada_nao_baixa_depois_da_reposicao() {
        estoqueRepository.save(new Estoque("ATRASADO", 1));
        Evento primeira = evento("pedido-atrasado", item("ATRASADO", 2));
        estoqueService.baixar(primeira);

        Estoque estoque = estoqueRepository.findByCodigoLivro("ATRASADO").orElseThrow();
        estoque.repor(5);
        estoqueRepository.save(estoque);
        Evento atrasada = evento("pedido-atrasado", item("ATRASADO", 2));
        estoqueService.baixar(atrasada);

        assertThat(primeira.getStatus()).isEqualTo(StatusSaga.ROLLBACK_PENDENTE);
        assertThat(atrasada.getStatus()).isNull();
        assertThat(disponivel("ATRASADO")).isEqualTo(6);
    }

    @Test
    void alteracao_feita_sobre_uma_leitura_antiga_e_recusada() {
        estoqueRepository.save(new Estoque("VERSAO", 5));
        Estoque primeiraLeitura = estoqueRepository.findByCodigoLivro("VERSAO").orElseThrow();
        Estoque segundaLeitura = estoqueRepository.findByCodigoLivro("VERSAO").orElseThrow();

        primeiraLeitura.repor(3);
        estoqueRepository.save(primeiraLeitura);
        segundaLeitura.repor(1);

        assertThatThrownBy(() -> estoqueRepository.save(segundaLeitura))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
        assertThat(disponivel("VERSAO")).isEqualTo(8);
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

    private int disponivel(String codigo) {
        return estoqueRepository.findByCodigoLivro(codigo).orElseThrow().getDisponivel();
    }

    private ItemPedido item(String codigo, int quantidade) {
        return new ItemPedido(new Livro(codigo, new BigDecimal("30.00")), quantidade);
    }

    private Evento evento(String pedidoId, ItemPedido... itens) {
        Pedido pedido = new Pedido();
        pedido.setId(pedidoId);
        pedido.setItens(List.of(itens));

        Evento evento = new Evento();
        evento.setVersao(Evento.VERSAO_ATUAL);
        evento.setPedidoId(pedidoId);
        evento.setTransacaoId("transacao-" + pedidoId);
        evento.setPedido(pedido);
        return evento;
    }
}
