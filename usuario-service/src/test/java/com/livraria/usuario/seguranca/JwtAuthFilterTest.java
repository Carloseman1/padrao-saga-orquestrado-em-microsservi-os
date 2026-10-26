package com.livraria.usuario.seguranca;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;

class JwtAuthFilterTest {

    private static final String SEGREDO = "segredo-de-teste-com-mais-de-32-caracteres-para-hs256";
    private static final long UMA_HORA = 3_600_000L;
    private static final String COOKIE = "livraria_sessao";
    private static final String EMAIL = "usuario@teste.com";

    private final JwtService jwtService = new JwtService(SEGREDO, UMA_HORA);
    private final TokensRevogados tokensRevogados = new TokensRevogados(jwtService, Clock.systemUTC());
    private final JwtAuthFilter filtro = new JwtAuthFilter(jwtService, tokensRevogados,
            new CookieDeSessao(COOKIE, false, UMA_HORA), new ObjectMapper());

    @Test
    void rota_publica_passa_sem_cookie() throws Exception {
        MockFilterChain cadeia = new MockFilterChain();

        filtro.doFilter(new MockHttpServletRequest("GET", "/api/endereco/01001000"),
                new MockHttpServletResponse(), cadeia);

        assertThat(cadeia.getRequest()).isNotNull();
    }

    @Test
    void rota_protegida_sem_cookie_responde_401() throws Exception {
        MockHttpServletResponse resposta = new MockHttpServletResponse();
        MockFilterChain cadeia = new MockFilterChain();

        filtro.doFilter(new MockHttpServletRequest("DELETE", "/api/sessao"), resposta, cadeia);

        assertThat(resposta.getStatus()).isEqualTo(401);
        assertThat(resposta.getContentAsString()).contains("TOKEN_INVALIDO");
        assertThat(cadeia.getRequest()).isNull();
    }

    @Test
    void token_valido_no_cookie_libera_e_identifica_o_usuario() throws Exception {
        MockHttpServletRequest requisicao = new MockHttpServletRequest("DELETE", "/api/sessao");
        requisicao.setCookies(new Cookie(COOKIE, jwtService.gerarToken(EMAIL, 7L, "CLIENTE")));
        MockFilterChain cadeia = new MockFilterChain();

        filtro.doFilter(requisicao, new MockHttpServletResponse(), cadeia);

        assertThat(cadeia.getRequest()).isNotNull();
        assertThat(requisicao.getAttribute(JwtAuthFilter.ATRIBUTO_ID)).isEqualTo(7L);
        assertThat(requisicao.getAttribute(JwtAuthFilter.ATRIBUTO_EMAIL)).isEqualTo(EMAIL);
        assertThat(requisicao.getAttribute(JwtAuthFilter.ATRIBUTO_PERFIL)).isEqualTo("CLIENTE");
    }

    @Test
    void token_no_header_authorization_nao_e_mais_aceito() throws Exception {
        MockHttpServletRequest requisicao = new MockHttpServletRequest("DELETE", "/api/sessao");
        requisicao.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + jwtService.gerarToken(EMAIL, 7L, "CLIENTE"));
        MockHttpServletResponse resposta = new MockHttpServletResponse();

        filtro.doFilter(requisicao, resposta, new MockFilterChain());

        assertThat(resposta.getStatus()).isEqualTo(401);
    }

    @Test
    void token_revogado_no_logout_e_recusado() throws Exception {
        String token = jwtService.gerarToken(EMAIL, 7L, "CLIENTE");
        tokensRevogados.revogar(token);
        MockHttpServletRequest requisicao = new MockHttpServletRequest("DELETE", "/api/sessao");
        requisicao.setCookies(new Cookie(COOKIE, token));
        MockHttpServletResponse resposta = new MockHttpServletResponse();

        filtro.doFilter(requisicao, resposta, new MockFilterChain());

        assertThat(resposta.getStatus()).isEqualTo(401);
    }
}