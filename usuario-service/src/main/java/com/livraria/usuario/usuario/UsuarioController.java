package com.livraria.usuario.usuario;

import com.livraria.usuario.seguranca.UsuarioAutenticado;
import com.livraria.usuario.usuario.dto.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.livraria.usuario.seguranca.CookieDeSessao;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final CookieDeSessao cookieDeSessao;
    private final UsuarioAutenticado usuarioAutenticado;

    public UsuarioController(UsuarioService usuarioService, CookieDeSessao cookieDeSessao,
                             UsuarioAutenticado usuarioAutenticado) {
        this.usuarioService = usuarioService;
        this.cookieDeSessao = cookieDeSessao;
        this.usuarioAutenticado = usuarioAutenticado;
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

    @PutMapping("/{id}/perfil")
    public UsuarioDTO alterarPerfil(@PathVariable Long id, @RequestBody PerfilRequest request) {
        usuarioAutenticado.exigirPerfil(Perfil.ADMIN.name());
        return usuarioService.alterarPerfil(id, request);
    }
}