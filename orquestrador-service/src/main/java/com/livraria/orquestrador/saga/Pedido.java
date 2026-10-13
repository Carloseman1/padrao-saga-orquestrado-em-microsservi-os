package com.livraria.orquestrador.saga;

import java.math.BigDecimal;
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
}
