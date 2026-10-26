package com.livraria.pagamento.cartao.dto;

import com.livraria.pagamento.cartao.Cartao;
import com.livraria.pagamento.cartao.NumeroCartao;

public record CartaoResumoDTO(
        Long id,
        String numeroMascarado,
        String nomeTitular,
        String validade) {

    public static CartaoResumoDTO de(Cartao cartao) {
        return new CartaoResumoDTO(
                cartao.getId(),
                NumeroCartao.mascarar(cartao.getUltimosQuatro()),
                cartao.getNomeTitular(),
                cartao.getValidade().formatada());
    }
}
