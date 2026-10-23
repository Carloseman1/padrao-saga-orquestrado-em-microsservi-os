package com.livraria.usuario.seguranca;

import java.time.Duration;

import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieDeSessao {

    private static final String SO_DO_MESMO_SITE = "Strict";
    private static final String TODAS_AS_ROTAS = "/";

    private final String nome;
    private final boolean seguro;
    public final Duration duracao;

    public CookieDeSessao(
            @Value("${app.sessao.cookie-nome}") String nome,
            @Value("${app.sessao.cookie-seguro}") boolean seguro,
            @Value("${app.jwt.expiration-ms}") Long expirationMs){
        this.nome  = nome;
        this.seguro = seguro;
        this.duracao = Duration.ofMillis(expirationMs);
    }

    public String nome(){
        return nome;
    }
    public ResponseCookie criar(String token){
        return montar(token, duracao);
    }

    public ResponseCookie expirar(){
        return montar("", Duration.ZERO);
    }

    private ResponseCookie montar(String valor, Duration validade){
        return ResponseCookie.from(nome, valor)
                .httpOnly(true)
                .secure(seguro)
                .sameSite(SO_DO_MESMO_SITE)
                .path(TODAS_AS_ROTAS)
                .maxAge(validade)
                .build();
    }
}
