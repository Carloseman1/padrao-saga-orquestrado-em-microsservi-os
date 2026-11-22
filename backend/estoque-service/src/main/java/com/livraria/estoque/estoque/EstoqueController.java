package com.livraria.estoque.estoque;

import java.util.List;

import com.livraria.estoque.seguranca.Perfis;
import com.livraria.estoque.seguranca.UsuarioAutenticado;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.livraria.estoque.estoque.dto.EntradaEstoqueRequest;
import com.livraria.estoque.estoque.dto.EstoqueDTO;

@RestController
@RequestMapping("/api/estoques")
public class EstoqueController {

    private final EstoqueService estoqueService;
    private final UsuarioAutenticado usuarioAutenticado;

    public EstoqueController(EstoqueService estoqueService, UsuarioAutenticado usuarioAutenticado) {
        this.estoqueService = estoqueService;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    @PostMapping("/entradas")
    public ResponseEntity<EstoqueDTO> registrarEntrada(@RequestBody EntradaEstoqueRequest request) {
        usuarioAutenticado.exigirPerfil(Perfis.OPERADOR, Perfis.ADMIN);
        EstoqueDTO atualizado = estoqueService.registrarEntrada(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(atualizado);
    }

    @GetMapping
    public List<EstoqueDTO> listar() {
        usuarioAutenticado.exigirPerfil(Perfis.OPERADOR, Perfis.ADMIN);
        return estoqueService.listar();
    }
}
