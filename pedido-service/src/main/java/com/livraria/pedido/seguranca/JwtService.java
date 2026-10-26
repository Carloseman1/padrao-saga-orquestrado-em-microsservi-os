package com.livraria.pedido.seguranca;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey secretKey;

    public JwtService(@Value("${app.jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public boolean tokenValido(String token) {
        try {
            lerClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public String extrairEmail(String token) {
        return lerClaims(token).getSubject();
    }

    public Long extrairUsuarioId(String token) {
        return lerClaims(token).get("usuarioId", Long.class);
    }

    private Claims lerClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}