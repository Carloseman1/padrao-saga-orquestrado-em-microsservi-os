package com.livraria.orquestrador.saga;

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
}
