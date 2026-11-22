package com.livraria.pedido.evento.dto;

public record FiltrosEvento(String pedidoId, String transacaoId) {

    public boolean temPedidoId() {
        return pedidoId != null && !pedidoId.isBlank();
    }

    public boolean temTransacaoId() {
        return transacaoId != null && !transacaoId.isBlank();
    }
}
