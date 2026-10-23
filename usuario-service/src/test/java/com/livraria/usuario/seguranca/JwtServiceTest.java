package com.livraria.usuario.seguranca;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String EMAIL = "usuario@teste.com";
    private static final String SEGREDO = "segredo-de-teste-com-mais-de-32-caracteres-para-hs256";
    private static final String OUTRO_SEGREDO = "outro-segredo-de-teste-com-mais-de-32-caracteres!!";
    private static final long UMA_HORA = 3_600_000L;

    @Test
    void gera_token_valido_com_o_email_no_assunto() {
        JwtService jwtService = new JwtService(SEGREDO, UMA_HORA);

        String token = jwtService.gerarToken(EMAIL, 1L);

        assertThat(jwtService.tokenValido(token)).isTrue();
        assertThat(jwtService.extrairEmail(token)).isEqualTo(EMAIL);
    }

    @Test
    void o_token_carrega_o_id_do_usuario() {
        JwtService jwtService = new JwtService(SEGREDO, UMA_HORA);

        String token = jwtService.gerarToken(EMAIL, 7L);

        assertThat(jwtService.extrairUsuarioId(token)).isEqualTo(7L);
    }

    @Test
    void token_expirado_nao_vale() {
        JwtService jaExpirado = new JwtService(SEGREDO, -1000L);

        String token = jaExpirado.gerarToken(EMAIL, 1L);

        assertThat(jaExpirado.tokenValido(token)).isFalse();
    }

    @Test
    void token_assinado_com_outro_segredo_nao_vale() {
        String token = new JwtService(SEGREDO, UMA_HORA).gerarToken(EMAIL, 1L);

        JwtService outraInstalacao = new JwtService(OUTRO_SEGREDO, UMA_HORA);

        assertThat(outraInstalacao.tokenValido(token)).isFalse();
    }

    @Test
    void texto_qualquer_nao_e_token() {
        JwtService jwtService = new JwtService(SEGREDO, UMA_HORA);

        assertThat(jwtService.tokenValido("nao-sou-um-token")).isFalse();
        assertThat(jwtService.tokenValido("")).isFalse();
    }
}