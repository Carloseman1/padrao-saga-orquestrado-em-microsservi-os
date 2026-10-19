package com.livraria.estoque.estoque;

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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.livraria.estoque.estoque.dto.EntradaEstoqueRequest;
import com.livraria.estoque.estoque.dto.EstoqueDTO;
import com.livraria.estoque.saga.Evento;
import com.livraria.estoque.saga.Historico;
import com.livraria.estoque.saga.ItemPedido;
import com.livraria.estoque.saga.Livro;
import com.livraria.estoque.saga.OrquestradorProducer;
import com.livraria.estoque.saga.Pedido;
import com.livraria.estoque.saga.StatusSaga;

@ExtendWith(MockitoExtension.class)
class EstoqueServiceTest {

    private static final Clock OUTUBRO_DE_2026 =
            Clock.fixed(Instant.parse("2026-10-12T10:00:00Z"), ZoneId.of("UTC"));
    private static final String PEDIDO = "pedido-1";
    private static final String TRANSACAO = "transacao-1";

    @Mock
    private EstoqueRepository estoqueRepository;

    @Mock
    private MovimentacaoEstoqueRepository movimentacaoRepository;

    @Mock
    private OrquestradorProducer orquestradorProducer;

    private EstoqueService estoqueService;

    @BeforeEach
    void criarService() {
        estoqueService = new EstoqueService(
                estoqueRepository, movimentacaoRepository, orquestradorProducer, OUTUBRO_DE_2026);
    }

    @Test
    void livro_novo_do_catalogo_ganha_estoque_com_a_quantidade_inicial() {
        when(estoqueRepository.findByCodigoLivro("O_ALIENISTA")).thenReturn(Optional.empty());

        estoqueService.criarParaLivroNovo(new LivroCadastrado("O_ALIENISTA", "O Alienista", 10));

        ArgumentCaptor<Estoque> capturado = ArgumentCaptor.forClass(Estoque.class);
        verify(estoqueRepository).save(capturado.capture());
        assertThat(capturado.getValue().getCodigoLivro()).isEqualTo("O_ALIENISTA");
        assertThat(capturado.getValue().getDisponivel()).isEqualTo(10);
    }

    @Test
    void livro_que_ja_tem_estoque_nao_e_criado_de_novo() {
        when(estoqueRepository.findByCodigoLivro("DOM_CASMURRO"))
                .thenReturn(Optional.of(new Estoque("DOM_CASMURRO", 4)));

        estoqueService.criarParaLivroNovo(new LivroCadastrado("DOM_CASMURRO", "Dom Casmurro", 10));

        verify(estoqueRepository, never()).save(any());
    }

    @Test
    void entrada_soma_ao_estoque_existente() {
        Estoque estoque = new Estoque("DOM_CASMURRO", 2);
        when(estoqueRepository.findByCodigoLivro("DOM_CASMURRO")).thenReturn(Optional.of(estoque));
        when(estoqueRepository.save(estoque)).thenReturn(estoque);

        EstoqueDTO atualizado = estoqueService.registrarEntrada(new EntradaEstoqueRequest("dom_casmurro", 3));

        assertThat(atualizado.disponivel()).isEqualTo(5);
    }

    @Test
    void baixa_estoque_e_registra_a_movimentacao() {
        Estoque estoque = new Estoque("DOM_CASMURRO", 4);
        when(estoqueRepository.findByCodigoLivro("DOM_CASMURRO")).thenReturn(Optional.of(estoque));
        Evento evento = evento(item("DOM_CASMURRO", 3));

        estoqueService.baixar(evento);

        assertThat(estoque.getDisponivel()).isEqualTo(1);
        assertThat(evento.getStatus()).isEqualTo(StatusSaga.SUCESSO);
        assertThat(evento.getOrigem()).isEqualTo("ESTOQUE_SERVICE");

        ArgumentCaptor<MovimentacaoEstoque> capturada = ArgumentCaptor.forClass(MovimentacaoEstoque.class);
        verify(movimentacaoRepository).save(capturada.capture());
        assertThat(capturada.getValue().getQuantidadeAnterior()).isEqualTo(4);
        assertThat(capturada.getValue().getQuantidadeNova()).isEqualTo(1);
        verify(orquestradorProducer).enviar(evento);
    }

    @Test
    void pede_rollback_quando_estoque_e_insuficiente() {
        Estoque estoque = new Estoque("O_CORTICO", 2);
        when(estoqueRepository.findByCodigoLivro("O_CORTICO")).thenReturn(Optional.of(estoque));
        Evento evento = evento(item("O_CORTICO", 3));

        estoqueService.baixar(evento);

        assertThat(estoque.getDisponivel()).isEqualTo(2);
        assertThat(evento.getStatus()).isEqualTo(StatusSaga.ROLLBACK_PENDENTE);
        assertThat(ultimaMensagem(evento))
                .isEqualTo("Falha ao atualizar o estoque: Estoque insuficiente para o livro.");
        verify(movimentacaoRepository, never()).save(any());
    }

    @Test
    void pede_rollback_quando_livro_nao_tem_estoque_cadastrado() {
        when(estoqueRepository.findByCodigoLivro("HARRY_POTTER")).thenReturn(Optional.empty());
        Evento evento = evento(item("HARRY_POTTER", 1));

        estoqueService.baixar(evento);

        assertThat(ultimaMensagem(evento)).isEqualTo("Falha ao atualizar o estoque: Livro sem estoque cadastrado.");
    }

    @Test
    void pede_rollback_quando_transacao_ja_movimentou_o_estoque() {
        when(movimentacaoRepository.existsByPedidoIdAndTransacaoId(PEDIDO, TRANSACAO)).thenReturn(true);
        Evento evento = evento(item("DOM_CASMURRO", 1));

        estoqueService.baixar(evento);

        assertThat(evento.getStatus()).isEqualTo(StatusSaga.ROLLBACK_PENDENTE);
        verify(estoqueRepository, never()).save(any());
    }

    @Test
    void rollback_devolve_a_quantidade_anterior() {
        Estoque estoque = new Estoque("DOM_CASMURRO", 4);
        MovimentacaoEstoque movimentacao =
                new MovimentacaoEstoque(estoque, PEDIDO, TRANSACAO, 3, LocalDateTime.now(OUTUBRO_DE_2026));
        estoque.baixar(3);
        when(movimentacaoRepository.findByPedidoIdAndTransacaoId(PEDIDO, TRANSACAO))
                .thenReturn(List.of(movimentacao));
        Evento evento = evento(item("DOM_CASMURRO", 3));

        estoqueService.desfazer(evento);

        assertThat(estoque.getDisponivel()).isEqualTo(4);
        assertThat(evento.getStatus()).isEqualTo(StatusSaga.FALHA);
        assertThat(ultimaMensagem(evento)).isEqualTo("Rollback executado no estoque.");
        verify(orquestradorProducer).enviar(evento);
    }

    private ItemPedido item(String codigo, int quantidade) {
        return new ItemPedido(new Livro(codigo, new BigDecimal("30.00")), quantidade);
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
