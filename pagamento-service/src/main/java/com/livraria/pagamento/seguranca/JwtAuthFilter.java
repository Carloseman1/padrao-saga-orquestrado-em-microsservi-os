package com.livraria.pagamento.seguranca;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.livraria.pagamento.seguranca.exceptions.TokenInvalidoException;
import com.livraria.pagamento.shared.exception.ErrorDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    public static final String ATRIBUTO_EMAIL = "usuarioEmail";
    public static final String ATRIBUTO_ID = "usuarioId";

    private static final List<String> PREFIXOS_PUBLICOS  = List.of(
            "GET /swagger-ui",
            "GET /v3/api-docs");

    private final JwtService jwtService;
    private final String nomeDoCookie;
    private final ObjectMapper objectMapper;

    public JwtAuthFilter(JwtService jwtService,
                         @Value("${app.sessao.cookie-nome}") String nomeDoCookie,
                         ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.nomeDoCookie = nomeDoCookie;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse
                                    response, FilterChain filterChain) throws ServletException, IOException{

        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || rotaPublica(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = tokenDoCookie(request);

        if(token == null || !jwtService.tokenValido(token)){
            recusar(response);
            return;
        }

        request.setAttribute(ATRIBUTO_EMAIL, jwtService.extrairEmail(token));
        request.setAttribute(ATRIBUTO_ID, jwtService.extrairUsuarioId(token));
        filterChain.doFilter(request,response);

    }

    private boolean rotaPublica(HttpServletRequest request){
        String assinatura =  request.getMethod() + " " + request.getRequestURI();

        return PREFIXOS_PUBLICOS.stream().anyMatch(assinatura::startsWith);
    }

    private String tokenDoCookie(HttpServletRequest request){
        Cookie cookie = WebUtils.getCookie(request, nomeDoCookie);

        return cookie == null || cookie.getValue().isBlank() ? null : cookie.getValue();
    }

    private void recusar(HttpServletResponse response) throws IOException{
        ErrorDTO erro = new TokenInvalidoException().toErrorDTO();

        response.setStatus(erro.status().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(erro));
    }

}
