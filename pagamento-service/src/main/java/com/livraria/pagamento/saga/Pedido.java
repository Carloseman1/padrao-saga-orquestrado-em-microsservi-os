package com.livraria.pagamento.saga;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class Pedido {

    private String id;
    private List<ItemPedido> itens;
    private String transacaoId;
    private BigDecimal valorTotal;
    private int quantidadeTotal;
    private LocalDateTime criadoEm;

    public BigDecimal calcularValorTotal() {
        return itens.stream()
                .map(item -> item.livro().valorUnitario().multiply(BigDecimal.valueOf(item.quantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_EVEN);
    }

    public int calcularQuantidadeTotal() {
        return itens.stream()
                .mapToInt(ItemPedido::quantidade)
                .sum();
    }
}
