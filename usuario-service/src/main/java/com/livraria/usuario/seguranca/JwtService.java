package com.livraria.usuario.seguranca;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String gerarToken(String email, Long usuarioId, String perfil) {
        Date agora = new Date();

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(email)
                .claim("usuarioId", usuarioId)
                .claim("perfil", perfil)
                .issuedAt(agora)
                .expiration(new Date(agora.getTime() + expirationMs))
                .signWith(secretKey)
                .compact();
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

    public String extrairJti(String token) {
        return lerClaims(token).getId();
    }

    public Instant extrairExpiracao(String token) {
        return lerClaims(token).getExpiration().toInstant();
    }

    public Long extrairUsuarioId(String token) {
        return lerClaims(token).get("usuarioId", Long.class);
    }

    public String extrairPerfil(String token) {
        return lerClaims(token).get("perfil", String.class);
    }

    private Claims lerClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}