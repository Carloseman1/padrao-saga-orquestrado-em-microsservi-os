package com.livraria.estoque.estoque;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "movimentacao_estoque")
@Data
@EqualsAndHashCode(of = "id")
public class MovimentacaoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "estoque_id", nullable = false)
    private Estoque estoque;

    @Column(name = "pedido_id", nullable = false)
    private String pedidoId;

    @Column(name = "transacao_id", nullable = false)
    private String transacaoId;

    @Column(name = "quantidade_pedida", nullable = false)
    private int quantidadePedida;

    @Column(name = "quantidade_anterior", nullable = false)
    private int quantidadeAnterior;

    @Column(name = "quantidade_nova", nullable = false)
    private int quantidadeNova;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    protected MovimentacaoEstoque() {
    }

    public MovimentacaoEstoque(Estoque estoque, String pedidoId, String transacaoId, int quantidadePedida,
                               LocalDateTime criadoEm) {
        this.estoque = estoque;
        this.pedidoId = pedidoId;
        this.transacaoId = transacaoId;
        this.quantidadePedida = quantidadePedida;
        this.quantidadeAnterior = estoque.getDisponivel();
        this.quantidadeNova = estoque.getDisponivel() - quantidadePedida;
        this.criadoEm = criadoEm;
    }
}
