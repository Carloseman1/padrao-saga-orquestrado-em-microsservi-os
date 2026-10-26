package com.livraria.pedido.pedido;

import com.livraria.pedido.seguranca.UsuarioAutenticado;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.livraria.pedido.pedido.dto.PedidoRequest;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;
    private final UsuarioAutenticado usuarioAutenticado;

    public PedidoController(PedidoService pedidoService, UsuarioAutenticado usuarioAutenticado) {
        this.pedidoService = pedidoService;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    @PostMapping
    public ResponseEntity<Pedido> criar(@RequestBody PedidoRequest request) {
        Pedido criado = pedidoService.criar(request, usuarioAutenticado.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }
}
