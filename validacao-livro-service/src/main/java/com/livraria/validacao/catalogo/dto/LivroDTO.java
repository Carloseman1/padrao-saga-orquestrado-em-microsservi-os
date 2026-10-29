package com.livraria.validacao.catalogo.dto;

import com.livraria.validacao.catalogo.Livro;

import java.math.BigDecimal;

public record LivroDTO(String codigo, String titulo, BigDecimal preco) {

    public static LivroDTO de(Livro livro) {
        return new LivroDTO(livro.getCodigo(), livro.getTitulo(), livro.getPreco());
    }
}