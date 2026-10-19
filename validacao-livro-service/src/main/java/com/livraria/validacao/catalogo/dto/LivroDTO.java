package com.livraria.validacao.catalogo.dto;

import com.livraria.validacao.catalogo.Livro;

public record LivroDTO(String codigo, String titulo) {

    public static LivroDTO de(Livro livro) {
        return new LivroDTO(livro.getCodigo(), livro.getTitulo());
    }
}
