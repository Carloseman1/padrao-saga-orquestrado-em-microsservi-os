package com.livraria.validacao.catalogo.dto;

import com.livraria.validacao.catalogo.CategoriaLivro;
import com.livraria.validacao.catalogo.Livro;

import java.math.BigDecimal;

public record LivroDTO(String codigo, String titulo, BigDecimal preco, String autor, String editora,
                       CategoriaLivro categoria, String descricao, String capaUrl) {

    public static LivroDTO de(Livro livro) {
        return new LivroDTO(livro.getCodigo(), livro.getTitulo(), livro.getPreco(), livro.getAutor(),
                livro.getEditora(), livro.getCategoria(), livro.getDescricao(), livro.getCapaUrl());
    }
}
