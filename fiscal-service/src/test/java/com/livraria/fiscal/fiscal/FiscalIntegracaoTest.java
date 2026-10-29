package com.livraria.fiscal.fiscal;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

import com.livraria.fiscal.saga.Evento;
import com.livraria.fiscal.saga.ItemPedido;
import com.livraria.fiscal.saga.Livro;
import com.livraria.fiscal.saga.OutboxRepository;
import com.livraria.fiscal.saga.Pedido;
import com.livraria.fiscal.saga.StatusSaga;

@SpringBootTest
@Testcontainers
class FiscalIntegracaoTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    @ServiceConnection
    static KafkaContainer kafka = new KafkaContainer("apache/kafka-native:3.8.1");

    @Autowired
    private FiscalService fiscalService;

    @Autowired
    private NotaFiscalRepository notaFiscalRepository;

    @Autowired
    private OutboxRepository outboxRepository;

    @Test
    void autoriza_a_nota_e_grava_a_resposta_junto() {
        Evento evento = evento("pedido-autorizado", "DOM_CASMURRO");

        fiscalService.emitir(evento);

        NotaFiscal nota = nota("pedido-autorizado");
        assertThat(evento.getStatus()).isEqualTo(StatusSaga.SUCESSO);
        assertThat(nota.getStatus()).isEqualTo(StatusNotaFiscal.AUTORIZADA);
        assertThat(nota.getChaveAcesso()).hasSize(44).startsWith("35");
        assertThat(nota.getValorTotal()).isEqualByComparingTo("79.80");
        assertThat(respostasNaOutbox("pedido-autorizado")).isEqualTo(1);
    }

    @Test
    void rejeicao_da_sefaz_pede_rollback_e_fica_registrada() {
        Evento evento = evento("pedido-rejeitado", "O_GUARANI");

        fiscalService.emitir(evento);

        assertThat(evento.getStatus()).isEqualTo(StatusSaga.ROLLBACK_PENDENTE);
        assertThat(nota("pedido-rejeitado").getStatus()).isEqualTo(StatusNotaFiscal.REJEITADA);
        assertThat(nota("pedido-rejeitado").getCodigoRetorno()).isEqualTo("778");
    }

    @Test
    void mensagem_duplicada_nao_emite_outra_nota() {
        fiscalService.emitir(evento("pedido-duplicado", "DOM_CASMURRO"));
        Evento duplicada = evento("pedido-duplicado", "DOM_CASMURRO");

        fiscalService.emitir(duplicada);

        assertThat(duplicada.getStatus()).isNull();
        assertThat(respostasNaOutbox("pedido-duplicado")).isEqualTo(1);
    }

    @Test
    void cancela_nota_autorizada_e_nao_mexe_na_rejeitada() {
        fiscalService.emitir(evento("pedido-cancelado", "DOM_CASMURRO"));
        fiscalService.emitir(evento("pedido-sem-nota", "O_GUARANI"));
        Evento cancelamento = evento("pedido-cancelado", "DOM_CASMURRO");
        Evento semNota = evento("pedido-sem-nota", "O_GUARANI");

        fiscalService.cancelar(cancelamento);
        fiscalService.cancelar(semNota);

        assertThat(nota("pedido-cancelado").getStatus()).isEqualTo(StatusNotaFiscal.CANCELADA);
        assertThat(cancelamento.getStatus()).isEqualTo(StatusSaga.FALHA);
        assertThat(nota("pedido-sem-nota").getStatus()).isEqualTo(StatusNotaFiscal.REJEITADA);
        assertThat(semNota.getStatus()).isEqualTo(StatusSaga.FALHA);
    }

    private NotaFiscal nota(String pedidoId) {
        return notaFiscalRepository.findByPedidoIdAndTransacaoId(pedidoId, "transacao-" + pedidoId).orElseThrow();
    }

    private long respostasNaOutbox(String pedidoId) {
        return outboxRepository.findAll().stream()
                .filter(mensagem -> mensagem.getChave().equals(pedidoId))
                .count();
    }

    private Evento evento(String pedidoId, String codigoDoLivro) {
        Pedido pedido = new Pedido();
        pedido.setId(pedidoId);
        pedido.setItens(List.of(new ItemPedido(new Livro(codigoDoLivro, new BigDecimal("39.90")), 2)));

        Evento evento = new Evento();
        evento.setVersao(Evento.VERSAO_ATUAL);
        evento.setPedidoId(pedidoId);
        evento.setTransacaoId("transacao-" + pedidoId);
        evento.setPedido(pedido);
        return evento;
    }
}
