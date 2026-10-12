package com.livraria.validacao.saga;

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

    public boolean temItens() {
        return itens != null && !itens.isEmpty();
    }
}
