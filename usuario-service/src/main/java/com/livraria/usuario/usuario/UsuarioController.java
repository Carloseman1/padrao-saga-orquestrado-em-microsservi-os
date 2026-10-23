package com.livraria.usuario.usuario;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.livraria.usuario.seguranca.CookieDeSessao;
import com.livraria.usuario.usuario.dto.LoginRequest;
import com.livraria.usuario.usuario.dto.LoginResponseDTO;
import com.livraria.usuario.usuario.dto.RecuperacaoRequest;
import com.livraria.usuario.usuario.dto.RedefinicaoRequest;
import com.livraria.usuario.usuario.dto.UsuarioDTO;
import com.livraria.usuario.usuario.dto.UsuarioRequest;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final CookieDeSessao cookieDeSessao;

    public UsuarioController(UsuarioService usuarioService, CookieDeSessao cookieDeSessao) {
        this.usuarioService = usuarioService;
        this.cookieDeSessao = cookieDeSessao;
    }

    @PostMapping
    public ResponseEntity<UsuarioDTO> cadastrar(@RequestBody UsuarioRequest request) {
        UsuarioDTO cadastrado = usuarioService.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(cadastrado);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequest request) {
        LoginResponseDTO login = usuarioService.login(request);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieDeSessao.criar(login.token()).toString())
                .body(login);
    }

    @PostMapping("/esqueci-senha")
    public ResponseEntity<Void> esqueciSenha(@RequestBody RecuperacaoRequest request) {
        usuarioService.solicitarRecuperacao(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/redefinir-senha")
    public ResponseEntity<Void> redefinirSenha(@RequestBody RedefinicaoRequest request) {
        usuarioService.redefinirSenha(request);
        return ResponseEntity.noContent().build();
    }
}