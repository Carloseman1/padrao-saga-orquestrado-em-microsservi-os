package com.livraria.pedido.pedido.dto;

import java.util.List;

import com.livraria.pedido.pedido.ItemPedido;

public record PedidoRequest(List<ItemPedido> itens, Long cartaoId) {
}
