package com.livraria.validacao.catalogo.dto;

import java.math.BigDecimal;

public record LivroRequest(String codigo, String titulo, BigDecimal preco, int quantidadeInicial) {
}