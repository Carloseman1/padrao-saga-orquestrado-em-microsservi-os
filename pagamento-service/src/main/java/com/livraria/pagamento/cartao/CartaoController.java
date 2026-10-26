package com.livraria.pagamento.cartao;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.livraria.pagamento.cartao.dto.CartaoAbertoDTO;
import com.livraria.pagamento.cartao.dto.CartaoRequest;
import com.livraria.pagamento.cartao.dto.CartaoResumoDTO;
import com.livraria.pagamento.seguranca.UsuarioAutenticado;

@RestController
@RequestMapping("/api/cartoes")
public class CartaoController {

    private final CartaoService cartaoService;
    private final UsuarioAutenticado usuarioAutenticado;

    public CartaoController(CartaoService cartaoService, UsuarioAutenticado usuarioAutenticado) {
        this.cartaoService = cartaoService;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    @PostMapping
    public ResponseEntity<CartaoResumoDTO> cadastrar(@RequestBody CartaoRequest request) {
        CartaoResumoDTO cadastrado = cartaoService.cadastrar(request, usuarioAutenticado.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(cadastrado);
    }

    @GetMapping
    public List<CartaoResumoDTO> listar() {
        return cartaoService.listar(usuarioAutenticado.id());
    }

    @GetMapping("/{id}/lerCartao")
    public CartaoAbertoDTO lerCartao(@PathVariable Long id) {
        return cartaoService.lerCartao(id, usuarioAutenticado.id(), usuarioAutenticado.email());
    }
}
