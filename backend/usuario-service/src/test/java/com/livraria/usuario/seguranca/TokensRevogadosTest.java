package com.livraria.usuario.seguranca;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

class TokensRevogadosTest {

    private static final String EMAIL = "usuario@teste.com";
    private static final String SEGREDO = "segredo-de-teste-com-mais-de-32-caracteres-para-hs256";
    private static final long UMA_HORA = 3_600_000L;
    private static final Instant AGORA = Instant.parse("2026-09-19T10:00:00Z");

    private final JwtService jwtService = new JwtService(SEGREDO, UMA_HORA);
    private final Clock relogio = Clock.fixed(AGORA, ZoneId.of("UTC"));
    private final TokensRevogados tokensRevogados = new TokensRevogados(jwtService, relogio);

    @Test
    void token_nao_revogado_continua_valendo() {
        String token = jwtService.gerarToken(EMAIL, 1L, "CLIENTE");

        assertThat(tokensRevogados.revogado(token)).isFalse();
    }

    @Test
    void token_revogado_para_de_valer() {
        String token = jwtService.gerarToken(EMAIL, 1L, "CLIENTE");

        tokensRevogados.revogar(token);

        assertThat(tokensRevogados.revogado(token)).isTrue();
    }

    @Test
    void revogar_um_token_nao_derruba_o_outro() {
        String doUsuario = jwtService.gerarToken(EMAIL, 1L, "CLIENTE");
        String daMaria = jwtService.gerarToken("maria@example.com", 2L, "CLIENTE");

        tokensRevogados.revogar(doUsuario);

        assertThat(tokensRevogados.revogado(daMaria)).isFalse();
    }

    @Test
    void token_que_ja_expirou_sai_da_lista() {
        String token = jwtService.gerarToken(EMAIL, 1L, "CLIENTE");
        tokensRevogados.revogar(token);

        Clock depoisDaExpiracao = Clock.offset(relogio, Duration.ofHours(2));
        TokensRevogados maisTarde = new TokensRevogados(jwtService, depoisDaExpiracao);
        maisTarde.revogar(jwtService.gerarToken("maria@example.com", 2L, "CLIENTE"));

        assertThat(maisTarde.revogado(token)).isFalse();
    }
}