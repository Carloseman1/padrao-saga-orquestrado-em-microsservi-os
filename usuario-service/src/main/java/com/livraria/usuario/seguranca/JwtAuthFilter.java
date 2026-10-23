package com.livraria.usuario.seguranca;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.livraria.usuario.shared.exception.ErrorDTO;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    public static final String ATRIBUTO_EMAIL = "usuarioEmail";
    public static final String ATRIBUTO_ID = "usuarioId";
    public static final String ATRIBUTO_TOKEN = "token";

    private static final Set<String> ROTAS_PUBLICAS = Set.of(
            "POST /api/usuarios",
            "POST /api/usuarios/login",
            "POST /api/usuarios/esqueci-senha",
            "POST /api/usuarios/redefinir-senha");

    private static final List<String> PREFIXOS_PUBLICOS = List.of(
            "GET /api/endereco/",
            "GET /swagger-ui",
            "GET /v3/api-docs");

    private final JwtService jwtService;
    private final TokensRevogados tokensRevogados;
    private final CookieDeSessao cookieDeSessao;
    private final ObjectMapper objectMapper;

    public JwtAuthFilter(JwtService jwtService, TokensRevogados tokensRevogados,
                         CookieDeSessao cookieDeSessao, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.tokensRevogados = tokensRevogados;
        this.cookieDeSessao = cookieDeSessao;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || rotaPublica(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = tokenDoCookie(request);

        if (token == null || !jwtService.tokenValido(token) || tokensRevogados.revogado(token)) {
            recusar(response);
            return;
        }

        request.setAttribute(ATRIBUTO_TOKEN, token);
        request.setAttribute(ATRIBUTO_EMAIL, jwtService.extrairEmail(token));
        request.setAttribute(ATRIBUTO_ID, jwtService.extrairUsuarioId(token));
        filterChain.doFilter(request, response);
    }

    private boolean rotaPublica(HttpServletRequest request) {
        String assinatura = request.getMethod() + " " + request.getRequestURI();

        return ROTAS_PUBLICAS.contains(assinatura)
                || PREFIXOS_PUBLICOS.stream().anyMatch(assinatura::startsWith);
    }

    private String tokenDoCookie(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, cookieDeSessao.nome());

        return cookie == null || cookie.getValue().isBlank() ? null : cookie.getValue();
    }

    private void recusar(HttpServletResponse response) throws IOException {
        ErrorDTO erro = new ErrorDTO(
                "TOKEN_INVALIDO",
                "Token ausente ou inválido. Faça login novamente.",
                HttpStatus.UNAUTHORIZED);

        response.setStatus(erro.status().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(erro));
    }
}