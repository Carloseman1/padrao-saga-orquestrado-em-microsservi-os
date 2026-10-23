package com.livraria.usuario.seguranca;

import io.jsonwebtoken.Jwt;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class TokensRevogados {

    private final Map<String, Instant> expiracaoPorToken = new ConcurrentHashMap<>();
    private final JwtService jwtService;
    private final Clock clock;

    public TokensRevogados(JwtService jwtService, Clock clock){
        this.jwtService = jwtService;
        this.clock = clock;
    }

    public void revogar(String token){
        esquecerOsQueJaExpiraram();
        expiracaoPorToken.put(jwtService.extrairJti(token),
        jwtService.extrairExpiracao(token));
    }

    public boolean revogado(String token){
        Instant expiraEm = expiracaoPorToken.get(jwtService.extrairJti(token));

        return expiraEm != null && expiraEm.isAfter(Instant.now(clock));
    }

    public void esquecerOsQueJaExpiraram(){
        expiracaoPorToken.values().removeIf(expiraEm ->
                !expiraEm.isAfter(Instant.now(clock)));
    }
}
