package com.livraria.pedido.pedido;

import java.time.LocalDateTime;

public record Tentativa(String transacaoId, Long cartaoId, StatusPedido resultado, String motivo,
                        LocalDateTime finalizadoEm) {
}
