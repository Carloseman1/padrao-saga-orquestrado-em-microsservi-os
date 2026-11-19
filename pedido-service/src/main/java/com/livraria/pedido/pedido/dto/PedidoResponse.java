package com.livraria.pedido.pedido.dto;

import com.livraria.pedido.pedido.ItemPedido;
import com.livraria.pedido.pedido.Pedido;
import com.livraria.pedido.pedido.StatusPedido;
import com.livraria.pedido.pedido.Tentativa;
import jdk.jshell.Snippet;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse (String id, StatusPedido status, String motivo, List<ItemPedido> itens,
                              BigDecimal valorTotal, Long cartaoId, String transacaoId, LocalDateTime criadoEm,
                              List<Tentativa> tentativas) {
    public static PedidoResponse de(Pedido pedido){
        return new PedidoResponse(pedido.getId(), pedido.getStatus(), pedido.getMotivo(), pedido.getItens(),
                pedido.getValorTotal(), pedido.getCartaoId(), pedido.getTransacaoId(), pedido.getCriadoEm(),
                pedido.getTentativas());
    }
}
