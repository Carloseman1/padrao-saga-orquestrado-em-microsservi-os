package com.livraria.estoque.estoque;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
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
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

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
    private BaixaEstoqueRepository baixaRepository;

    @Mock
    private OrquestradorProducer orquestradorProducer;

    private EstoqueService estoqueService;

    @BeforeEach
    void criarService() {
        TransactionTemplate transacao = new TransactionTemplate(mock(PlatformTransactionManager.class));
        estoqueService = new EstoqueService(
                estoqueRepository, movimentacaoRepository, baixaRepository, orquestradorProducer, transacao,
                OUTUBRO_DE_2026);
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
        assertThat(baixaGravada().isSucesso()).isTrue();
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
        assertThat(baixaGravada().isSucesso()).isFalse();
    }

    @Test
    void pede_rollback_quando_livro_nao_tem_estoque_cadastrado() {
        when(estoqueRepository.findByCodigoLivro("HARRY_POTTER")).thenReturn(Optional.empty());
        Evento evento = evento(item("HARRY_POTTER", 1));

        estoqueService.baixar(evento);

        assertThat(ultimaMensagem(evento)).isEqualTo("Falha ao atualizar o estoque: Livro sem estoque cadastrado.");
    }

    @Test
    void ignora_mensagem_duplicada_sem_responder_ao_orquestrador() {
        when(baixaRepository.existsByPedidoIdAndTransacaoId(PEDIDO, TRANSACAO)).thenReturn(true);
        Evento evento = evento(item("DOM_CASMURRO", 1));

        estoqueService.baixar(evento);

        assertThat(evento.getStatus()).isNull();
        verify(estoqueRepository, never()).save(any());
        verify(orquestradorProducer, never()).enviar(any());
    }

    @Test
    void conflito_de_versao_na_baixa_volta_para_o_kafka_tentar_de_novo() {
        Estoque estoque = new Estoque("DOM_CASMURRO", 4);
        when(estoqueRepository.findByCodigoLivro("DOM_CASMURRO")).thenReturn(Optional.of(estoque));
        when(estoqueRepository.save(estoque))
                .thenThrow(new ObjectOptimisticLockingFailureException(Estoque.class, 1L));
        Evento evento = evento(item("DOM_CASMURRO", 1));

        assertThatThrownBy(() -> estoqueService.baixar(evento))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);

        assertThat(evento.getStatus()).isNull();
        verify(orquestradorProducer, never()).enviar(any());
    }

    @Test
    void conflito_de_versao_no_rollback_volta_para_o_kafka_tentar_de_novo() {
        Estoque estoque = new Estoque("DOM_CASMURRO", 2);
        MovimentacaoEstoque movimentacao = baixaDoPedido(estoque, 1);
        when(movimentacaoRepository.findByPedidoIdAndTransacaoId(PEDIDO, TRANSACAO))
                .thenReturn(List.of(movimentacao));
        when(estoqueRepository.save(estoque))
                .thenThrow(new ObjectOptimisticLockingFailureException(Estoque.class, 1L));
        Evento evento = evento(item("DOM_CASMURRO", 1));

        assertThatThrownBy(() -> estoqueService.desfazer(evento))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);

        verify(orquestradorProducer, never()).enviar(any());
    }

    @Test
    void rollback_devolve_o_pedido_sem_apagar_venda_feita_no_meio() {
        Estoque estoque = new Estoque("DOM_CASMURRO", 2);
        MovimentacaoEstoque movimentacao = baixaDoPedido(estoque, 1);
        estoque.baixar(1);
        when(movimentacaoRepository.findByPedidoIdAndTransacaoId(PEDIDO, TRANSACAO))
                .thenReturn(List.of(movimentacao));
        Evento evento = evento(item("DOM_CASMURRO", 1));

        estoqueService.desfazer(evento);

        assertThat(estoque.getDisponivel()).isEqualTo(1);
        assertThat(movimentacao.isEstornada()).isTrue();
        assertThat(evento.getStatus()).isEqualTo(StatusSaga.FALHA);
        assertThat(ultimaMensagem(evento)).isEqualTo("Rollback executado no estoque.");
        verify(movimentacaoRepository).save(movimentacao);
        verify(orquestradorProducer).enviar(evento);
    }

    @Test
    void rollback_devolve_cada_item_quando_o_mesmo_livro_aparece_duas_vezes() {
        Estoque estoque = new Estoque("DOM_CASMURRO", 2);
        MovimentacaoEstoque primeira = baixaDoPedido(estoque, 1);
        MovimentacaoEstoque segunda = baixaDoPedido(estoque, 1);
        when(movimentacaoRepository.findByPedidoIdAndTransacaoId(PEDIDO, TRANSACAO))
                .thenReturn(List.of(primeira, segunda));

        estoqueService.desfazer(evento(item("DOM_CASMURRO", 1), item("DOM_CASMURRO", 1)));

        assertThat(estoque.getDisponivel()).isEqualTo(2);
    }

    @Test
    void rollback_repetido_nao_devolve_de_novo() {
        Estoque estoque = new Estoque("DOM_CASMURRO", 2);
        MovimentacaoEstoque movimentacao = baixaDoPedido(estoque, 1);
        when(movimentacaoRepository.findByPedidoIdAndTransacaoId(PEDIDO, TRANSACAO))
                .thenReturn(List.of(movimentacao));
        Evento evento = evento(item("DOM_CASMURRO", 1));

        estoqueService.desfazer(evento);
        estoqueService.desfazer(evento);

        assertThat(estoque.getDisponivel()).isEqualTo(2);
    }

    private BaixaEstoque baixaGravada() {
        ArgumentCaptor<BaixaEstoque> gravada = ArgumentCaptor.forClass(BaixaEstoque.class);
        verify(baixaRepository, atLeastOnce()).save(gravada.capture());
        return gravada.getValue();
    }

    private MovimentacaoEstoque baixaDoPedido(Estoque estoque, int quantidade) {
        MovimentacaoEstoque movimentacao =
                new MovimentacaoEstoque(estoque, PEDIDO, TRANSACAO, quantidade,
                        LocalDateTime.now(OUTUBRO_DE_2026));
        estoque.baixar(quantidade);
        return movimentacao;
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
