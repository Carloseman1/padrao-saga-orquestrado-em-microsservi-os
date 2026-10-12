package com.livraria.pedido.pedido;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.livraria.pedido.pedido.exceptions.PedidoSemItensException;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Document(collection = "pedido")
@Data
@EqualsAndHashCode(of = "id")
public class Pedido {

    @Id
    private String id;
    private List<ItemPedido> itens;
    private String transacaoId;
    private double valorTotal;
    private int quantidadeTotal;
    private LocalDateTime criadoEm;

    protected Pedido() {
    }

    public Pedido(List<ItemPedido> itens, String transacaoId, LocalDateTime criadoEm) {
        if (itens == null || itens.isEmpty()) {
            throw new PedidoSemItensException();
        }
        this.itens = itens;
        this.transacaoId = transacaoId;
        this.criadoEm = criadoEm;
    }
}
