package com.livraria.usuario.usuario;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.livraria.usuario.usuario.exceptions.MuitasTentativasException;

class ControleDeTentativasTest {

    private static final String EMAIL = "usuario@teste.com";
    private static final long BLOQUEIO_EM_SEGUNDOS = 60;

    private RelogioAjustavel relogio;
    private ControleDeTentativas controle;

    @BeforeEach
    void criarControle() {
        relogio = new RelogioAjustavel(Instant.parse("2026-09-18T10:00:00Z"));
        controle = new ControleDeTentativas(BLOQUEIO_EM_SEGUNDOS, relogio);
    }

    @Test
    void quatro_falhas_ainda_deixam_tentar() {
        falhar(4);

        assertThatCode(() -> controle.exigirLiberado(EMAIL)).doesNotThrowAnyException();
    }

    @Test
    void a_quinta_falha_bloqueia() {
        falhar(5);

        assertThatThrownBy(() -> controle.exigirLiberado(EMAIL))
                .isInstanceOf(MuitasTentativasException.class);
    }

    @Test
    void o_bloqueio_cai_depois_do_tempo() {
        falhar(5);

        relogio.avancar(Duration.ofSeconds(BLOQUEIO_EM_SEGUNDOS + 1));

        assertThatCode(() -> controle.exigirLiberado(EMAIL)).doesNotThrowAnyException();
    }

    @Test
    void acerto_zera_a_contagem() {
        falhar(4);
        controle.registrarAcerto(EMAIL);
        falhar(4);

        assertThatCode(() -> controle.exigirLiberado(EMAIL)).doesNotThrowAnyException();
    }

    @Test
    void bloqueio_e_por_email() {
        falhar(5);

        assertThatCode(() -> controle.exigirLiberado("outra.pessoa@example.com"))
                .doesNotThrowAnyException();
    }

    private void falhar(int vezes) {
        for (int i = 0; i < vezes; i++) {
            controle.registrarFalha(EMAIL);
        }
    }

    private static class RelogioAjustavel extends Clock {

        private Instant agora;

        RelogioAjustavel(Instant inicio) {
            this.agora = inicio;
        }

        void avancar(Duration duracao) {
            agora = agora.plus(duracao);
        }

        @Override
        public Instant instant() {
            return agora;
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
    }
}