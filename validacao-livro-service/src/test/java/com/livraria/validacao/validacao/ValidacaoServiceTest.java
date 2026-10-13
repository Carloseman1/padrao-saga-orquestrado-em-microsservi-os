package com.livraria.validacao.validacao;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.livraria.validacao.catalogo.LivroRepository;
import com.livraria.validacao.saga.Evento;
import com.livraria.validacao.saga.Historico;
import com.livraria.validacao.saga.ItemPedido;
import com.livraria.validacao.saga.Livro;
import com.livraria.validacao.saga.OrquestradorProducer;
import com.livraria.validacao.saga.Pedido;
import com.livraria.validacao.saga.StatusSaga;

@ExtendWith(MockitoExtension.class)
class ValidacaoServiceTest {

    private static final Clock OUTUBRO_DE_2026 =
            Clock.fixed(Instant.parse("2026-10-12T10:00:00Z"), ZoneId.of("UTC"));
    private static final String PEDIDO = "pedido-1";
    private static final String TRANSACAO = "transacao-1";

    @Mock
    private LivroRepository livroRepository;

    @Mock
    private ValidacaoRepository validacaoRepository;

    @Mock
    private OrquestradorProducer orquestradorProducer;

    private ValidacaoService validacaoService;

    @BeforeEach
    void criarService() {
        validacaoService = new ValidacaoService(
                livroRepository, validacaoRepository, orquestradorProducer, OUTUBRO_DE_2026);
    }

    @Test
    void valida_pedido_com_livros_cadastrados() {
        when(livroRepository.existsByCodigo("DOM_CASMURRO")).thenReturn(true);
        Evento evento = evento(item("DOM_CASMURRO"));

        validacaoService.validar(evento);

        assertThat(evento.getStatus()).isEqualTo(StatusSaga.SUCESSO);
        assertThat(evento.getOrigem()).isEqualTo("VALIDACAO_LIVRO_SERVICE");
        assertThat(ultimaMensagem(evento)).isEqualTo("Livros validados com sucesso.");
        verify(validacaoRepository).save(any(Validacao.class));
        verify(orquestradorProducer).enviar(evento);
    }

    @Test
    void pede_rollback_quando_livro_nao_esta_no_catalogo() {
        when(livroRepository.existsByCodigo("HARRY_POTTER")).thenReturn(false);
        Evento evento = evento(item("HARRY_POTTER"));

        validacaoService.validar(evento);

        assertThat(evento.getStatus()).isEqualTo(StatusSaga.ROLLBACK_PENDENTE);
        assertThat(ultimaMensagem(evento)).isEqualTo("Falha ao validar os livros: Livro não cadastrado.");
        verify(validacaoRepository, never()).save(any());
        verify(orquestradorProducer).enviar(evento);
    }

    @Test
    void pede_rollback_quando_pedido_nao_tem_itens() {
        Evento evento = evento();

        validacaoService.validar(evento);

        assertThat(evento.getStatus()).isEqualTo(StatusSaga.ROLLBACK_PENDENTE);
        assertThat(ultimaMensagem(evento)).isEqualTo("Falha ao validar os livros: O pedido não possui livros.");
    }

    @Test
    void pede_rollback_quando_item_nao_informa_o_livro() {
        Evento evento = evento(new ItemPedido(null, 1));

        validacaoService.validar(evento);

        assertThat(ultimaMensagem(evento))
                .isEqualTo("Falha ao validar os livros: Informe o livro de cada item do pedido.");
    }

    @Test
    void pede_rollback_quando_transacao_ja_foi_validada() {
        when(validacaoRepository.existsByPedidoIdAndTransacaoId(PEDIDO, TRANSACAO)).thenReturn(true);
        Evento evento = evento(item("DOM_CASMURRO"));

        validacaoService.validar(evento);

        assertThat(ultimaMensagem(evento))
                .isEqualTo("Falha ao validar os livros: Já existe uma validação para esta transação.");
    }

    @Test
    void rollback_marca_validacao_existente_como_falha() {
        Validacao existente = new Validacao(PEDIDO, TRANSACAO, true, LocalDateTime.now(OUTUBRO_DE_2026));
        when(validacaoRepository.findByPedidoIdAndTransacaoId(PEDIDO, TRANSACAO)).thenReturn(Optional.of(existente));
        Evento evento = evento(item("DOM_CASMURRO"));

        validacaoService.desfazer(evento);

        assertThat(existente.isSucesso()).isFalse();
        assertThat(evento.getStatus()).isEqualTo(StatusSaga.FALHA);
        verify(validacaoRepository).save(existente);
        verify(orquestradorProducer).enviar(evento);
    }

    @Test
    void rollback_registra_falha_quando_nao_havia_validacao() {
        when(validacaoRepository.findByPedidoIdAndTransacaoId(PEDIDO, TRANSACAO)).thenReturn(Optional.empty());

        validacaoService.desfazer(evento(item("DOM_CASMURRO")));

        ArgumentCaptor<Validacao> capturada = ArgumentCaptor.forClass(Validacao.class);
        verify(validacaoRepository).save(capturada.capture());
        assertThat(capturada.getValue().isSucesso()).isFalse();
        assertThat(capturada.getValue().getPedidoId()).isEqualTo(PEDIDO);
    }

    private ItemPedido item(String codigo) {
        return new ItemPedido(new Livro(codigo, new BigDecimal("30.00")), 1);
    }

    private Evento evento(ItemPedido... itens) {
        Pedido pedido = new Pedido();
        pedido.setId(PEDIDO);
        pedido.setItens(new ArrayList<>(List.of(itens)));

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
