package com.livraria.orquestrador.orquestracao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.livraria.orquestrador.orquestracao.exceptions.OrigemOuStatusNaoInformadoException;
import com.livraria.orquestrador.saga.Evento;
import com.livraria.orquestrador.saga.OrigemEvento;
import com.livraria.orquestrador.saga.SagaProducer;
import com.livraria.orquestrador.saga.StatusSaga;

@ExtendWith(MockitoExtension.class)
class OrquestracaoServiceTest {

    private static final Clock OUTUBRO_DE_2026 =
            Clock.fixed(Instant.parse("2026-10-12T10:00:00Z"), ZoneId.of("UTC"));

    @Mock
    private SagaProducer sagaProducer;

    private OrquestracaoService orquestracaoService;

    @BeforeEach
    void criarService() {
        orquestracaoService = new OrquestracaoService(sagaProducer, OUTUBRO_DE_2026);
    }

    @Test
    void inicia_a_saga_enviando_para_a_validacao_dos_livros() {
        Evento evento = new Evento();

        orquestracaoService.iniciar(evento);

        assertThat(evento.getOrigem()).isEqualTo(OrigemEvento.ORQUESTRADOR);
        assertThat(evento.getStatus()).isEqualTo(StatusSaga.SUCESSO);
        assertThat(evento.getHistorico()).hasSize(1);
        verify(sagaProducer).enviar(evento, Topico.VALIDACAO_LIVRO_SUCESSO);
    }

    @Test
    void continua_a_saga_pela_origem_e_status_do_evento() {
        Evento evento = new Evento();
        evento.setOrigem(OrigemEvento.PAGAMENTO_SERVICE);
        evento.setStatus(StatusSaga.ROLLBACK_PENDENTE);

        orquestracaoService.continuar(evento);

        verify(sagaProducer).enviar(evento, Topico.PAGAMENTO_FALHA);
    }

    @Test
    void recusa_evento_sem_origem_ou_status() {
        assertThatThrownBy(() -> orquestracaoService.continuar(new Evento()))
                .isInstanceOf(OrigemOuStatusNaoInformadoException.class);

        verifyNoInteractions(sagaProducer);
    }

    @Test
    void finaliza_com_sucesso_notificando_o_pedido() {
        Evento evento = new Evento();

        orquestracaoService.finalizarComSucesso(evento);

        assertThat(evento.getStatus()).isEqualTo(StatusSaga.SUCESSO);
        verify(sagaProducer).enviar(evento, Topico.NOTIFICAR_FIM);
    }

    @Test
    void finaliza_com_falha_notificando_o_pedido() {
        Evento evento = new Evento();

        orquestracaoService.finalizarComFalha(evento);

        assertThat(evento.getStatus()).isEqualTo(StatusSaga.FALHA);
        verify(sagaProducer).enviar(evento, Topico.NOTIFICAR_FIM);
    }
}
