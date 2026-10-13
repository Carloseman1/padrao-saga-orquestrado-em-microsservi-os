package com.livraria.pedido.evento;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

import com.livraria.pedido.evento.dto.FiltrosEvento;
import com.livraria.pedido.evento.exceptions.EventoNaoEncontradoException;
import com.livraria.pedido.evento.exceptions.FiltroNaoInformadoException;
import com.livraria.pedido.pedido.ItemPedido;
import com.livraria.pedido.pedido.Livro;
import com.livraria.pedido.pedido.Pedido;

@ExtendWith(MockitoExtension.class)
class EventoServiceTest {

    private static final Clock OUTUBRO_DE_2026 =
            Clock.fixed(Instant.parse("2026-10-12T10:00:00Z"), ZoneId.of("UTC"));

    @Mock
    private EventoRepository eventoRepository;

    private EventoService eventoService;

    @BeforeEach
    void criarService() {
        eventoService = new EventoService(eventoRepository, OUTUBRO_DE_2026);
    }

    @Test
    void busca_pelo_pedido_quando_informado() {
        Evento evento = evento("pedido-1");
        when(eventoRepository.findTop1ByPedidoIdOrderByCriadoEmDesc("pedido-1")).thenReturn(Optional.of(evento));

        assertThat(eventoService.buscar(new FiltrosEvento("pedido-1", null))).isEqualTo(evento);
    }

    @Test
    void busca_pela_transacao_quando_pedido_nao_informado() {
        Evento evento = evento("pedido-1");
        when(eventoRepository.findTop1ByTransacaoIdOrderByCriadoEmDesc("transacao-1"))
                .thenReturn(Optional.of(evento));

        assertThat(eventoService.buscar(new FiltrosEvento(" ", "transacao-1"))).isEqualTo(evento);
    }

    @Test
    void recusa_busca_sem_filtros() {
        assertThatThrownBy(() -> eventoService.buscar(new FiltrosEvento(null, "")))
                .isInstanceOf(FiltroNaoInformadoException.class);

        verifyNoInteractions(eventoRepository);
    }

    @Test
    void avisa_quando_evento_nao_existe() {
        when(eventoRepository.findTop1ByPedidoIdOrderByCriadoEmDesc("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventoService.buscar(new FiltrosEvento("inexistente", null)))
                .isInstanceOf(EventoNaoEncontradoException.class);
    }

    @Test
    void notificar_fim_atualiza_pedido_e_data_do_evento() {
        Evento evento = evento("pedido-1");
        evento.setPedidoId(null);

        eventoService.notificarFim(evento);

        assertThat(evento.getPedidoId()).isEqualTo("pedido-1");
        assertThat(evento.getCriadoEm()).isEqualTo(LocalDateTime.now(OUTUBRO_DE_2026));
        verify(eventoRepository).save(evento);
    }

    @Test
    void lista_eventos_do_mais_recente_para_o_mais_antigo() {
        List<Evento> eventos = List.of(evento("pedido-2"), evento("pedido-1"));
        when(eventoRepository.findAllByOrderByCriadoEmDesc()).thenReturn(eventos);

        assertThat(eventoService.listar()).isEqualTo(eventos);
    }

    private Evento evento(String pedidoId) {
        Pedido pedido = new Pedido(
                List.of(new ItemPedido(new Livro("IRACEMA", new BigDecimal("25.00")), 1)), "transacao-1", LocalDateTime.now(OUTUBRO_DE_2026));
        pedido.setId(pedidoId);
        return new Evento(pedido, LocalDateTime.now(OUTUBRO_DE_2026));
    }
}
