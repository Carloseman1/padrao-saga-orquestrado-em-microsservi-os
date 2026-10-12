package com.livraria.pedido.evento;

import java.time.LocalDateTime;

public record Historico(String origem, String status, String mensagem, LocalDateTime criadoEm) {
}
