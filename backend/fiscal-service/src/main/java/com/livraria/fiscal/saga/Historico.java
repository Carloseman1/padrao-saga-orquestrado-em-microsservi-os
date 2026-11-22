package com.livraria.fiscal.saga;

import java.time.LocalDateTime;

public record Historico(String origem, StatusSaga status, String mensagem, LocalDateTime criadoEm) {
}
