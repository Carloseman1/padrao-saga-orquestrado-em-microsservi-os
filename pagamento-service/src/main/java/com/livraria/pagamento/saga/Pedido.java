package com.livraria.pagamento.saga;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class Pedido {

    private String id;
    private List<ItemPedido> itens;
    private String transacaoId;
    private double valorTotal;
    private int quantidadeTotal;
    private LocalDateTime criadoEm;

    public double calcularValorTotal() {
        return itens.stream()
                .mapToDouble(item -> item.quantidade() * item.livro().valorUnitario())
                .sum();
    }

    public int calcularQuantidadeTotal() {
        return itens.stream()
                .mapToInt(ItemPedido::quantidade)
                .sum();
    }
}
