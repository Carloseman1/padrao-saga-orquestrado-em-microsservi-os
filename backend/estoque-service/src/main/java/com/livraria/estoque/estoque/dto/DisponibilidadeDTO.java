package com.livraria.estoque.estoque.dto;

import com.livraria.estoque.estoque.Estoque;
import com.livraria.estoque.estoque.SituacaoEstoque;

public record DisponibilidadeDTO(String codigoLivro, SituacaoEstoque situacao) {

    public static DisponibilidadeDTO de(Estoque estoque) {
        return new DisponibilidadeDTO(estoque.getCodigoLivro(), SituacaoEstoque.de(estoque.getDisponivel()));
    }
}
