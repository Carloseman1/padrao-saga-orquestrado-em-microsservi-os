package com.livraria.pedido.pedido;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.livraria.pedido.evento.Evento;
import com.livraria.pedido.evento.EventoService;
import com.livraria.pedido.pedido.dto.PedidoRequest;
import com.livraria.pedido.pedido.exceptions.PedidoSemItensException;
import com.livraria.pedido.saga.SagaProducer;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    private static final Clock OUTUBRO_DE_2026 =
            Clock.fixed(Instant.parse("2026-10-12T10:00:00Z"), ZoneId.of("UTC"));
    private static final ItemPedido DOM_CASMURRO = new ItemPedido(new Livro("DOM_CASMURRO", 39.9), 2);

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private EventoService eventoService;

    @Mock
    private SagaProducer sagaProducer;

    private PedidoService pedidoService;

    @BeforeEach
    void criarService() {
        pedidoService = new PedidoService(pedidoRepository, eventoService, sagaProducer, OUTUBRO_DE_2026);
    }

    @Test
    void cria_pedido_com_transacao_e_data_do_relogio() {
        devolverOPedidoSalvoComId("pedido-1");
        devolverOEventoSalvo();

        Pedido pedido = pedidoService.criar(new PedidoRequest(List.of(DOM_CASMURRO)));

        assertThat(pedido.getId()).isEqualTo("pedido-1");
        assertThat(pedido.getItens()).containsExactly(DOM_CASMURRO);
        assertThat(pedido.getTransacaoId()).startsWith(OUTUBRO_DE_2026.millis() + "_");
        assertThat(pedido.getCriadoEm()).isEqualTo(LocalDateTime.now(OUTUBRO_DE_2026));
    }

    @Test
    void inicia_a_saga_com_o_evento_do_pedido_salvo() {
        devolverOPedidoSalvoComId("pedido-1");
        devolverOEventoSalvo();

        Pedido pedido = pedidoService.criar(new PedidoRequest(List.of(DOM_CASMURRO)));

        ArgumentCaptor<Evento> capturado = ArgumentCaptor.forClass(Evento.class);
        verify(sagaProducer).iniciar(capturado.capture());
        Evento evento = capturado.getValue();

        assertThat(evento.getPedidoId()).isEqualTo("pedido-1");
        assertThat(evento.getTransacaoId()).isEqualTo(pedido.getTransacaoId());
        assertThat(evento.getPedido()).isEqualTo(pedido);
    }

    @Test
    void recusa_pedido_sem_itens() {
        assertThatThrownBy(() -> pedidoService.criar(new PedidoRequest(List.of())))
                .isInstanceOf(PedidoSemItensException.class);

        verify(pedidoRepository, never()).save(any());
        verify(sagaProducer, never()).iniciar(any());
    }

    @Test
    void recusa_pedido_com_itens_nulos() {
        assertThatThrownBy(() -> pedidoService.criar(new PedidoRequest(null)))
                .isInstanceOf(PedidoSemItensException.class);
    }

    private void devolverOPedidoSalvoComId(String id) {
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocacao -> {
            Pedido pedido = invocacao.getArgument(0);
            pedido.setId(id);
            return pedido;
        });
    }

    private void devolverOEventoSalvo() {
        when(eventoService.salvar(any(Evento.class))).thenAnswer(invocacao -> invocacao.getArgument(0));
    }
}
