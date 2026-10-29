package com.livraria.validacao.seguranca;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;

class JwtAuthFilterTest {

    private static final String SEGREDO = "segredo-de-teste-com-mais-de-32-caracteres-para-hs256";
    private static final String OUTRO_SEGREDO = "outro-segredo-de-teste-com-mais-de-32-caracteres!!";
    private static final String COOKIE = "livraria_sessao";
    private static final String EMAIL = "usuario@teste.com";
    private static final long UM_MINUTO = 60_000L;

    private final JwtAuthFilter filtro =
            new JwtAuthFilter(new JwtService(SEGREDO), COOKIE, new ObjectMapper());

    @Test
    void swagger_e_publico() throws Exception {
        MockFilterChain cadeia = new MockFilterChain();

        filtro.doFilter(new MockHttpServletRequest("GET", "/swagger-ui/index.html"),
                new MockHttpServletResponse(), cadeia);

        assertThat(cadeia.getRequest()).isNotNull();
    }

    @Test
    void criar_pedido_sem_cookie_responde_401() throws Exception {
        MockHttpServletResponse resposta = new MockHttpServletResponse();
        MockFilterChain cadeia = new MockFilterChain();

        filtro.doFilter(new MockHttpServletRequest("POST", "/api/pedidos"), resposta, cadeia);

        assertThat(resposta.getStatus()).isEqualTo(401);
        assertThat(resposta.getContentAsString()).contains("TOKEN_INVALIDO");
        assertThat(cadeia.getRequest()).isNull();
    }

    @Test
    void token_do_usuario_service_no_cookie_libera_e_identifica_o_usuario() throws Exception {
        MockHttpServletRequest requisicao = new MockHttpServletRequest("POST", "/api/pedidos");
        requisicao.setCookies(new Cookie(COOKIE, token(SEGREDO, UM_MINUTO)));
        MockFilterChain cadeia = new MockFilterChain();

        filtro.doFilter(requisicao, new MockHttpServletResponse(), cadeia);

        assertThat(cadeia.getRequest()).isNotNull();
        assertThat(requisicao.getAttribute(JwtAuthFilter.ATRIBUTO_ID)).isEqualTo(7L);
        assertThat(requisicao.getAttribute(JwtAuthFilter.ATRIBUTO_EMAIL)).isEqualTo(EMAIL);
    }

    @Test
    void token_assinado_com_outro_segredo_e_recusado() throws Exception {
        MockHttpServletRequest requisicao = new MockHttpServletRequest("POST", "/api/pedidos");
        requisicao.setCookies(new Cookie(COOKIE, token(OUTRO_SEGREDO, UM_MINUTO)));
        MockHttpServletResponse resposta = new MockHttpServletResponse();

        filtro.doFilter(requisicao, resposta, new MockFilterChain());

        assertThat(resposta.getStatus()).isEqualTo(401);
    }

    @Test
    void token_expirado_e_recusado() throws Exception {
        MockHttpServletRequest requisicao = new MockHttpServletRequest("GET", "/api/eventos/todos");
        requisicao.setCookies(new Cookie(COOKIE, token(SEGREDO, -UM_MINUTO)));
        MockHttpServletResponse resposta = new MockHttpServletResponse();

        filtro.doFilter(requisicao, resposta, new MockFilterChain());

        assertThat(resposta.getStatus()).isEqualTo(401);
    }

    @Test
    void token_no_header_authorization_nao_e_aceito() throws Exception {
        MockHttpServletRequest requisicao = new MockHttpServletRequest("POST", "/api/pedidos");
        requisicao.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token(SEGREDO, UM_MINUTO));
        MockHttpServletResponse resposta = new MockHttpServletResponse();

        filtro.doFilter(requisicao, resposta, new MockFilterChain());

        assertThat(resposta.getStatus()).isEqualTo(401);
    }

    private String token(String segredo, long validadeEmMs) {
        return Jwts.builder()
                .subject(EMAIL)
                .claim("usuarioId", 7L)
                .expiration(new Date(System.currentTimeMillis() + validadeEmMs))
                .signWith(Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}