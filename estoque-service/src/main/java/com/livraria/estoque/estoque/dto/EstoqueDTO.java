package com.livraria.estoque.estoque.dto;

import com.livraria.estoque.estoque.Estoque;

public record EstoqueDTO(String codigoLivro, int disponivel) {

    public static EstoqueDTO de(Estoque estoque) {
        return new EstoqueDTO(estoque.getCodigoLivro(), estoque.getDisponivel());
    }
}
