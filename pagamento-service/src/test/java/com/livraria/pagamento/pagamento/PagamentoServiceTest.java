package com.livraria.pagamento.pagamento;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.livraria.pagamento.saga.Evento;
import com.livraria.pagamento.saga.Historico;
import com.livraria.pagamento.saga.ItemPedido;
import com.livraria.pagamento.saga.Livro;
import com.livraria.pagamento.saga.OrquestradorProducer;
import com.livraria.pagamento.saga.Pedido;
import com.livraria.pagamento.saga.StatusSaga;

@ExtendWith(MockitoExtension.class)
class PagamentoServiceTest {

    private static final Clock OUTUBRO_DE_2026 =
            Clock.fixed(Instant.parse("2026-10-12T10:00:00Z"), ZoneId.of("UTC"));
    private static final String PEDIDO = "pedido-1";
    private static final String TRANSACAO = "transacao-1";

    @Mock
    private PagamentoRepository pagamentoRepository;

    @Mock
    private OrquestradorProducer orquestradorProducer;

    private PagamentoService pagamentoService;

    @BeforeEach
    void criarService() {
        pagamentoService = new PagamentoService(pagamentoRepository, orquestradorProducer, OUTUBRO_DE_2026);
    }

    @Test
    void realiza_pagamento_e_preenche_os_totais_do_pedido() {
        devolverOPagamentoSalvo();
        Evento evento = evento(new ItemPedido(new Livro("DOM_CASMURRO", new BigDecimal("30.00")), 2),
                new ItemPedido(new Livro("IRACEMA", new BigDecimal("15.50")), 1));

        pagamentoService.realizar(evento);

        assertThat(evento.getStatus()).isEqualTo(StatusSaga.SUCESSO);
        assertThat(evento.getOrigem()).isEqualTo("PAGAMENTO_SERVICE");
        assertThat(evento.getPedido().getValorTotal()).isEqualByComparingTo("75.50");
        assertThat(evento.getPedido().getQuantidadeTotal()).isEqualTo(3);
        verify(orquestradorProducer).enviar(evento);
    }

    @Test
    void soma_os_centavos_sem_erro_de_arredondamento() {
        devolverOPagamentoSalvo();
        Evento evento = evento(new ItemPedido(new Livro("O_CORTICO", new BigDecimal("29.90")), 3));

        pagamentoService.realizar(evento);

        assertThat(evento.getPedido().getValorTotal()).isEqualTo(new BigDecimal("89.70"));
    }

    @Test
    void pede_rollback_quando_valor_esta_abaixo_do_minimo() {
        devolverOPagamentoSalvo();
        Evento evento = evento(new ItemPedido(new Livro("DOM_CASMURRO", new BigDecimal("0.05")), 1));

        pagamentoService.realizar(evento);

        assertThat(evento.getStatus()).isEqualTo(StatusSaga.ROLLBACK_PENDENTE);
        assertThat(ultimaMensagem(evento))
                .isEqualTo("Falha ao realizar o pagamento: O valor mínimo para pagamento é R$ 0,10.");
        verify(orquestradorProducer).enviar(evento);
    }

    @Test
    void pede_rollback_quando_transacao_ja_foi_paga() {
        when(pagamentoRepository.existsByPedidoIdAndTransacaoId(PEDIDO, TRANSACAO)).thenReturn(true);
        Evento evento = evento(new ItemPedido(new Livro("DOM_CASMURRO", new BigDecimal("30.00")), 1));

        pagamentoService.realizar(evento);

        assertThat(evento.getStatus()).isEqualTo(StatusSaga.ROLLBACK_PENDENTE);
        verify(pagamentoRepository, never()).save(any());
    }

    @Test
    void estorna_pagamento_existente() {
        Pagamento pagamento = new Pagamento(PEDIDO, TRANSACAO, 2, new BigDecimal("60.00"), LocalDateTime.now(OUTUBRO_DE_2026));
        when(pagamentoRepository.findByPedidoIdAndTransacaoId(PEDIDO, TRANSACAO)).thenReturn(Optional.of(pagamento));
        Evento evento = evento(new ItemPedido(new Livro("DOM_CASMURRO", new BigDecimal("30.00")), 2));

        pagamentoService.estornar(evento);

        assertThat(pagamento.getStatus()).isEqualTo(StatusPagamento.ESTORNADO);
        assertThat(evento.getStatus()).isEqualTo(StatusSaga.FALHA);
        assertThat(evento.getPedido().getValorTotal()).isEqualByComparingTo("60.00");
        assertThat(ultimaMensagem(evento)).isEqualTo("Estorno do pagamento realizado.");
    }

    @Test
    void registra_falha_quando_nao_ha_pagamento_para_estornar() {
        when(pagamentoRepository.findByPedidoIdAndTransacaoId(PEDIDO, TRANSACAO)).thenReturn(Optional.empty());
        Evento evento = evento(new ItemPedido(new Livro("DOM_CASMURRO", new BigDecimal("30.00")), 1));

        pagamentoService.estornar(evento);

        assertThat(evento.getStatus()).isEqualTo(StatusSaga.FALHA);
        assertThat(ultimaMensagem(evento)).isEqualTo(
                "Estorno do pagamento não realizado: Pagamento não encontrado para o pedido e a transação.");
        verify(orquestradorProducer).enviar(evento);
    }

    private void devolverOPagamentoSalvo() {
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(invocacao -> invocacao.getArgument(0));
    }

    private Evento evento(ItemPedido... itens) {
        Pedido pedido = new Pedido();
        pedido.setId(PEDIDO);
        pedido.setItens(List.of(itens));

        Evento evento = new Evento();
        evento.setTransacaoId(TRANSACAO);
        evento.setPedido(pedido);
        return evento;
    }

    private String ultimaMensagem(Evento evento) {
        List<Historico> historico = evento.getHistorico();
        return historico.get(historico.size() - 1).mensagem();
    }
}
