package com.livraria.pagamento.cartao.dto;

public record CartaoAbertoDTO(
        Long id,
        String numero,
        String nomeTitular,
        String validade,
        String cvv) {
}
