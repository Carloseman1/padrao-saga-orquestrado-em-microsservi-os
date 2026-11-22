package com.livraria.pedido.pedido;

import com.livraria.pedido.pedido.dto.NovaTentativaRequest;
import com.livraria.pedido.pedido.dto.PedidoResponse;
import com.livraria.pedido.seguranca.UsuarioAutenticado;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<PedidoResponse> criar(@RequestBody PedidoRequest request) {
        Pedido criado = pedidoService.criar(request, usuarioAutenticado.id());
        return ResponseEntity.accepted().body(PedidoResponse.de(criado));
    }

    @GetMapping("/{id}")
    public PedidoResponse buscar(@PathVariable String id){
        return PedidoResponse.de(pedidoService.buscar(id, usuarioAutenticado.id()));
    }

    @PostMapping("/{id}/tentativas")
    public ResponseEntity<PedidoResponse> tentarDeNovo(@PathVariable String id,
                                                       @RequestBody NovaTentativaRequest request){
        Pedido pedido = pedidoService.tentarDeNovo(id, request, usuarioAutenticado.id());
        return ResponseEntity.accepted().body(PedidoResponse.de(pedido));
    }
}
