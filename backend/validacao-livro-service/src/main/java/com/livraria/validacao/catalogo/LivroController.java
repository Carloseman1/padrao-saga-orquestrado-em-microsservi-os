package com.livraria.validacao.catalogo;

import java.util.List;

import com.livraria.validacao.seguranca.Perfis;
import com.livraria.validacao.seguranca.UsuarioAutenticado;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.livraria.validacao.catalogo.dto.LivroDTO;
import com.livraria.validacao.catalogo.dto.LivroRequest;

@RestController
@RequestMapping("/api/livros")
public class LivroController {

    private final LivroService livroService;
    private final UsuarioAutenticado usuarioAutenticado;

    public LivroController(LivroService livroService, UsuarioAutenticado usuarioAutenticado) {
        this.livroService = livroService;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    @PostMapping
    public ResponseEntity<LivroDTO> cadastrar(@RequestBody LivroRequest request) {
        usuarioAutenticado.exigirPerfil(Perfis.OPERADOR, Perfis.ADMIN);
        LivroDTO cadastrado = livroService.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(cadastrado);
    }

    @GetMapping
    public List<LivroDTO> listar() {
        return livroService.listar();
    }
}
