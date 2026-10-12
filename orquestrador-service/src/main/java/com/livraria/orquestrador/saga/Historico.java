package com.livraria.orquestrador.saga;

import java.time.LocalDateTime;

public record Historico(OrigemEvento origem, StatusSaga status, String mensagem, LocalDateTime criadoEm) {
}
