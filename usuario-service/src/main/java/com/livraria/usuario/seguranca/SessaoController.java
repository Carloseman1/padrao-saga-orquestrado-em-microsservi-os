package com.livraria.usuario.seguranca;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sessao")
public class SessaoController {

    private final TokensRevogados tokensRevogados;
    private final UsuarioAutenticado usuarioAutenticado;
    private final CookieDeSessao cookieDeSessao;

    public SessaoController(TokensRevogados tokensRevogados, UsuarioAutenticado usuarioAutenticado,
                            CookieDeSessao cookieDeSessao){
        this.tokensRevogados = tokensRevogados;
        this.usuarioAutenticado = usuarioAutenticado;
        this.cookieDeSessao = cookieDeSessao;
    }

    @DeleteMapping
    public ResponseEntity<Void> sair() {
        tokensRevogados.revogar(usuarioAutenticado.token());

        return  ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookieDeSessao.expirar().toString())
                .build();
    }
}
