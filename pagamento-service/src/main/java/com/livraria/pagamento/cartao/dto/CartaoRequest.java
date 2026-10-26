package com.livraria.pagamento.cartao.dto;

public record CartaoRequest(
        String numero,
        String nomeTitular,
        int validadeMes,
        int validadeAno,
        String cvv) {
}
