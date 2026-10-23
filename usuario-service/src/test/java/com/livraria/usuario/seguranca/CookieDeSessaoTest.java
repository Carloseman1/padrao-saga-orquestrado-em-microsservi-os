package com.livraria.usuario.seguranca;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

class CookieDeSessaoTest {

    private static final String NOME = "livraria_sessao";
    private static final long UMA_HORA = 3_600_000L;

    private final CookieDeSessao cookieDeSessao = new CookieDeSessao(NOME, false, UMA_HORA);

    @Test
    void cookie_do_login_guarda_o_token_fora_do_alcance_do_javascript() {
        ResponseCookie cookie = cookieDeSessao.criar("token-de-teste");

        assertThat(cookie.getName()).isEqualTo(NOME);
        assertThat(cookie.getValue()).isEqualTo("token-de-teste");
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getSameSite()).isEqualTo("Strict");
        assertThat(cookie.getPath()).isEqualTo("/");
    }

    @Test
    void cookie_vence_junto_com_o_token() {
        assertThat(cookieDeSessao.criar("token-de-teste").getMaxAge()).isEqualTo(Duration.ofHours(1));
    }

    @Test
    void cookie_do_logout_manda_o_navegador_apagar() {
        ResponseCookie cookie = cookieDeSessao.expirar();

        assertThat(cookie.getValue()).isEmpty();
        assertThat(cookie.getMaxAge()).isZero();
    }

    @Test
    void cookie_seguro_so_trafega_por_https() {
        CookieDeSessao producao = new CookieDeSessao(NOME, true, UMA_HORA);

        assertThat(producao.criar("token-de-teste").isSecure()).isTrue();
    }
}