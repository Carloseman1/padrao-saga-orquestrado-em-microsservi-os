package com.livraria.pedido.pedido;

import java.math.BigDecimal;

import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

public record Livro(String codigo, @Field(targetType = FieldType.DECIMAL128) BigDecimal valorUnitario) {
}
