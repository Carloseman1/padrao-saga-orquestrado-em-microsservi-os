package com.livraria.pedido.evento;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.livraria.pedido.evento.dto.FiltrosEvento;
import com.livraria.pedido.evento.exceptions.EventoNaoEncontradoException;
import com.livraria.pedido.evento.exceptions.FiltroNaoInformadoException;
import com.livraria.pedido.pedido.ItemPedido;
import com.livraria.pedido.pedido.Livro;
import com.livraria.pedido.pedido.Pedido;
import com.livraria.pedido.pedido.PedidoRepository;
import com.livraria.pedido.pedido.StatusPedido;
import com.livraria.pedido.pedido.exceptions.PedidoNaoEncontradoException;

@ExtendWith(MockitoExtension.class)
class EventoServiceTest {

    private static final Clock OUTUBRO_DE_2026 =
            Clock.fixed(Instant.parse("2026-10-12T10:00:00Z"), ZoneId.of("UTC"));
    private static final Long USUARIO = 7L;
    private static final Long OUTRO_USUARIO = 8L;

    @Mock
    private EventoRepository eventoRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    private EventoService eventoService;

    @BeforeEach
    void criarService() {
        eventoService = new EventoService(eventoRepository, pedidoRepository,
                new TransactionTemplate(mock(PlatformTransactionManager.class)), OUTUBRO_DE_2026);
    }

    @Test
    void busca_pelo_pedido_do_usuario_quando_informado() {
        Evento evento = evento(pedido("pedido-1"));
        when(eventoRepository.findTop1ByPedidoIdAndPedidoUsuarioIdOrderByCriadoEmDesc("pedido-1", USUARIO))
                .thenReturn(Optional.of(evento));

        assertThat(eventoService.buscar(new FiltrosEvento("pedido-1", null), USUARIO)).isEqualTo(evento);
    }

    @Test
    void busca_pela_transacao_quando_pedido_nao_informado() {
        Evento evento = evento(pedido("pedido-1"));
        when(eventoRepository.findTop1ByTransacaoIdAndPedidoUsuarioIdOrderByCriadoEmDesc("transacao-1", USUARIO))
                .thenReturn(Optional.of(evento));

        assertThat(eventoService.buscar(new FiltrosEvento(" ", "transacao-1"), USUARIO)).isEqualTo(evento);
    }

    @Test
    void pedido_de_outro_usuario_responde_como_nao_encontrado() {
        when(eventoRepository.findTop1ByPedidoIdAndPedidoUsuarioIdOrderByCriadoEmDesc("pedido-1", OUTRO_USUARIO))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventoService.buscar(new FiltrosEvento("pedido-1", null), OUTRO_USUARIO))
                .isInstanceOf(EventoNaoEncontradoException.class);
    }

    @Test
    void recusa_busca_sem_filtros() {
        assertThatThrownBy(() -> eventoService.buscar(new FiltrosEvento(null, ""), USUARIO))
                .isInstanceOf(FiltroNaoInformadoException.class);

        verifyNoInteractions(eventoRepository);
    }

    @Test
    void avisa_quando_evento_nao_existe() {
        when(eventoRepository.findTop1ByPedidoIdAndPedidoUsuarioIdOrderByCriadoEmDesc("inexistente", USUARIO))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventoService.buscar(new FiltrosEvento("inexistente", null), USUARIO))
                .isInstanceOf(EventoNaoEncontradoException.class);
    }

    @Test
    void notificar_fim_atualiza_pedido_e_data_do_evento() {
        Pedido pedido = pedido("pedido-1");
        Evento evento = fimComSucesso(pedido);
        evento.setPedidoId(null);
        when(pedidoRepository.findById("pedido-1")).thenReturn(Optional.of(pedido));

        eventoService.notificarFim(evento);

        assertThat(evento.getPedidoId()).isEqualTo("pedido-1");
        assertThat(evento.getCriadoEm()).isEqualTo(LocalDateTime.now(OUTUBRO_DE_2026));
        verify(eventoRepository).save(evento);
    }

    @Test
    void saga_com_sucesso_conclui_o_pedido() {
        Pedido pedido = pedido("pedido-1");
        when(pedidoRepository.findById("pedido-1")).thenReturn(Optional.of(pedido));

        eventoService.notificarFim(fimComSucesso(pedido));

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CONCLUIDO);
        assertThat(pedido.getMotivo()).isNull();
        assertThat(pedido.getTentativas()).singleElement()
                .satisfies(tentativa -> assertThat(tentativa.resultado()).isEqualTo(StatusPedido.CONCLUIDO));
        verify(pedidoRepository).save(pedido);
    }

    @Test
    void pagamento_recusado_deixa_o_cliente_tentar_outro_cartao() {
        Pedido pedido = pedido("pedido-1");
        when(pedidoRepository.findById("pedido-1")).thenReturn(Optional.of(pedido));

        eventoService.notificarFim(fimComFalhaEm(pedido, "PAGAMENTO_SERVICE", "Falha ao realizar o pagamento: Cartão vencido."));

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.PAGAMENTO_RECUSADO);
        assertThat(pedido.getMotivo()).isEqualTo("Falha ao realizar o pagamento: Cartão vencido.");
        verify(pedidoRepository).save(pedido);
    }

    @Test
    void falha_no_estoque_encerra_o_pedido() {
        Pedido pedido = pedido("pedido-1");
        when(pedidoRepository.findById("pedido-1")).thenReturn(Optional.of(pedido));

        eventoService.notificarFim(fimComFalhaEm(pedido, "ESTOQUE_SERVICE", "Estoque insuficiente para o livro."));

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.FALHOU);
        assertThat(pedido.getMotivo()).isEqualTo("Estoque insuficiente para o livro.");
    }

    @Test
    void fim_de_uma_tentativa_antiga_nao_muda_o_pedido() {
        Pedido pedido = pedido("pedido-1");
        Evento atrasado = fimComFalhaEm(pedido, "PAGAMENTO_SERVICE", "Cartão vencido.");
        atrasado.setTransacaoId("transacao-antiga");
        when(pedidoRepository.findById("pedido-1")).thenReturn(Optional.of(pedido));

        eventoService.notificarFim(atrasado);

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.PROCESSANDO);
        assertThat(pedido.getTentativas()).isEmpty();
        verify(eventoRepository).save(atrasado);
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void fim_repetido_da_mesma_tentativa_nao_muda_o_pedido() {
        Pedido pedido = pedido("pedido-1");
        when(pedidoRepository.findById("pedido-1")).thenReturn(Optional.of(pedido));
        eventoService.notificarFim(fimComSucesso(pedido));

        eventoService.notificarFim(fimComFalhaEm(pedido, "PAGAMENTO_SERVICE", "Cartão vencido."));

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CONCLUIDO);
        assertThat(pedido.getTentativas()).hasSize(1);
    }

    @Test
    void fim_de_pedido_que_nao_existe_falha_para_tentar_de_novo() {
        Evento evento = fimComSucesso(pedido("inexistente"));
        when(pedidoRepository.findById("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventoService.notificarFim(evento))
                .isInstanceOf(PedidoNaoEncontradoException.class);
    }

    @Test
    void lista_so_os_eventos_do_usuario_do_mais_recente_para_o_mais_antigo() {
        List<Evento> eventos = List.of(evento(pedido("pedido-2")), evento(pedido("pedido-1")));
        when(eventoRepository.findAllByPedidoUsuarioIdOrderByCriadoEmDesc(USUARIO)).thenReturn(eventos);

        assertThat(eventoService.listar(USUARIO)).isEqualTo(eventos);
    }

    private Pedido pedido(String pedidoId) {
        Pedido pedido = new Pedido(
                List.of(new ItemPedido(new Livro("IRACEMA", new BigDecimal("25.00")), 1)),
                USUARIO, 1L, "transacao-1", LocalDateTime.now(OUTUBRO_DE_2026));
        pedido.setId(pedidoId);
        return pedido;
    }

    private Evento evento(Pedido pedido) {
        return new Evento(pedido, LocalDateTime.now(OUTUBRO_DE_2026));
    }

    private Evento fimComSucesso(Pedido pedido) {
        Evento evento = evento(pedido);
        evento.setOrigem("ORQUESTRADOR");
        evento.setStatus("SUCESSO");
        evento.setHistorico(List.of(registro("ORQUESTRADOR", "SUCESSO", "Saga finalizada com sucesso.")));
        return evento;
    }

    private Evento fimComFalhaEm(Pedido pedido, String origem, String mensagem) {
        Evento evento = evento(pedido);
        evento.setOrigem("ORQUESTRADOR");
        evento.setStatus("FALHA");
        evento.setHistorico(List.of(
                registro("VALIDACAO_LIVRO_SERVICE", "SUCESSO", "Livros validados com sucesso."),
                registro(origem, "ROLLBACK_PENDENTE", mensagem),
                registro(origem, "FALHA", "Etapa desfeita."),
                registro("ORQUESTRADOR", "FALHA", "Saga finalizada com erros.")));
        return evento;
    }

    private Historico registro(String origem, String status, String mensagem) {
        return new Historico(origem, status, mensagem, LocalDateTime.now(OUTUBRO_DE_2026));
    }
}
