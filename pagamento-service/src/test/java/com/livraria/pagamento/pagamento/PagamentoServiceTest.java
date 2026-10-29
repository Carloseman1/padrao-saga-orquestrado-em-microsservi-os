package com.livraria.pagamento.pagamento;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.livraria.pagamento.cartao.CartaoService;
import com.livraria.pagamento.cartao.exceptions.CartaoNaoEncontradoException;
import com.livraria.pagamento.cartao.exceptions.CartaoVencidoException;
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
    private static final Long USUARIO = 7L;
    private static final Long CARTAO = 1L;

    @Mock
    private PagamentoRepository pagamentoRepository;

    @Mock
    private OrquestradorProducer orquestradorProducer;

    @Mock
    private CartaoService cartaoService;

    private PagamentoService pagamentoService;

    @BeforeEach
    void criarService() {
        pagamentoService = new PagamentoService(pagamentoRepository, cartaoService, orquestradorProducer,
                new TransactionTemplate(mock(PlatformTransactionManager.class)), OUTUBRO_DE_2026);
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
        Evento evento = evento(new ItemPedido(new Livro("DOM_CASMURRO", new BigDecimal("0.05")), 1));

        pagamentoService.realizar(evento);

        assertThat(evento.getStatus()).isEqualTo(StatusSaga.ROLLBACK_PENDENTE);
        assertThat(ultimaMensagem(evento))
                .isEqualTo("Falha ao realizar o pagamento: O valor mínimo para pagamento é R$ 0,10.");
        assertThat(statusGravado()).isEqualTo(StatusPagamento.RECUSADO);
        verify(orquestradorProducer).enviar(evento);
    }

    @Test
    void ignora_mensagem_duplicada_sem_responder_ao_orquestrador() {
        when(pagamentoRepository.existsByPedidoIdAndTransacaoId(PEDIDO, TRANSACAO)).thenReturn(true);
        Evento evento = evento(new ItemPedido(new Livro("DOM_CASMURRO", new BigDecimal("30.00")), 1));

        pagamentoService.realizar(evento);

        assertThat(evento.getStatus()).isNull();
        verify(cartaoService, never()).exigirValidoParaPagamento(any(), any());
        verify(pagamentoRepository, never()).save(any());
        verify(orquestradorProducer, never()).enviar(any());
    }

    @Test
    void ignora_duplicata_recusada_pelo_banco() {
        when(pagamentoRepository.save(any(Pagamento.class)))
                .thenThrow(new DataIntegrityViolationException("uk_pagamento_pedido_transacao"));
        Evento evento = evento(new ItemPedido(new Livro("DOM_CASMURRO", new BigDecimal("30.00")), 1));

        pagamentoService.realizar(evento);

        verify(orquestradorProducer, never()).enviar(any());
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
    void nao_estorna_pagamento_que_foi_recusado() {
        Pagamento recusado = new Pagamento(PEDIDO, TRANSACAO, 1, new BigDecimal("0.05"), LocalDateTime.now(OUTUBRO_DE_2026));
        recusado.recusar(LocalDateTime.now(OUTUBRO_DE_2026));
        when(pagamentoRepository.findByPedidoIdAndTransacaoId(PEDIDO, TRANSACAO)).thenReturn(Optional.of(recusado));
        Evento evento = evento(new ItemPedido(new Livro("DOM_CASMURRO", new BigDecimal("0.05")), 1));

        pagamentoService.estornar(evento);

        assertThat(recusado.getStatus()).isEqualTo(StatusPagamento.RECUSADO);
        assertThat(evento.getStatus()).isEqualTo(StatusSaga.FALHA);
        assertThat(ultimaMensagem(evento)).isEqualTo("Pagamento recusado, não há o que estornar.");
        verify(pagamentoRepository, never()).save(any());
        verify(orquestradorProducer).enviar(evento);
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

    @Test
    void pede_rollback_quando_o_cartao_nao_e_do_usuario() {
        doThrow(new CartaoNaoEncontradoException()).when(cartaoService).exigirValidoParaPagamento(CARTAO, USUARIO);
        Evento evento = evento(new ItemPedido(new Livro("DOM_CASMURRO", new BigDecimal("30.00")), 1));

        pagamentoService.realizar(evento);

        assertThat(evento.getStatus()).isEqualTo(StatusSaga.ROLLBACK_PENDENTE);
        assertThat(ultimaMensagem(evento)).isEqualTo("Falha ao realizar o pagamento: Cartão não encontrado.");
        assertThat(statusGravado()).isEqualTo(StatusPagamento.RECUSADO);
    }

    @Test
    void pede_rollback_quando_o_cartao_esta_vencido() {
        doThrow(new CartaoVencidoException()).when(cartaoService).exigirValidoParaPagamento(CARTAO, USUARIO);
        Evento evento = evento(new ItemPedido(new Livro("DOM_CASMURRO", new BigDecimal("30.00")), 1));

        pagamentoService.realizar(evento);

        assertThat(evento.getStatus()).isEqualTo(StatusSaga.ROLLBACK_PENDENTE);
        assertThat(ultimaMensagem(evento)).isEqualTo("Falha ao realizar o pagamento: O cartão já está vencido.");
        assertThat(statusGravado()).isEqualTo(StatusPagamento.RECUSADO);
    }

    private StatusPagamento statusGravado() {
        ArgumentCaptor<Pagamento> gravado = ArgumentCaptor.forClass(Pagamento.class);
        verify(pagamentoRepository).save(gravado.capture());
        return gravado.getValue().getStatus();
    }

    private void devolverOPagamentoSalvo() {
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(invocacao -> invocacao.getArgument(0));
    }

    private Evento evento(ItemPedido... itens) {
        Pedido pedido = new Pedido();
        pedido.setId(PEDIDO);
        pedido.setItens(List.of(itens));
        pedido.setUsuarioId(USUARIO);
        pedido.setCartaoId(CARTAO);

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
