package com.livraria.estoque.estoque;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "baixa_estoque")
@Data
@EqualsAndHashCode(of = "id")
public class BaixaEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pedido_id", nullable = false)
    private String pedidoId;

    @Column(name = "transacao_id", nullable = false)
    private String transacaoId;

    @Column(name = "sucesso", nullable = false)
    private boolean sucesso;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    protected BaixaEstoque() {
    }

    public BaixaEstoque(String pedidoId, String transacaoId, boolean sucesso, LocalDateTime criadoEm) {
        this.pedidoId = pedidoId;
        this.transacaoId = transacaoId;
        this.sucesso = sucesso;
        this.criadoEm = criadoEm;
    }
}
