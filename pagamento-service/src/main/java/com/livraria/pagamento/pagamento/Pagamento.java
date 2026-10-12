package com.livraria.pagamento.pagamento;

import java.time.LocalDateTime;

import com.livraria.pagamento.pagamento.exceptions.ValorMinimoNaoAtingidoException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "pagamento")
@Data
@EqualsAndHashCode(of = "id")
public class Pagamento {

    private static final double VALOR_MINIMO = 0.1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pedido_id", nullable = false)
    private String pedidoId;

    @Column(name = "transacao_id", nullable = false)
    private String transacaoId;

    @Column(name = "quantidade_total", nullable = false)
    private int quantidadeTotal;

    @Column(name = "valor_total", nullable = false)
    private double valorTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusPagamento status;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    protected Pagamento() {
    }

    public Pagamento(String pedidoId, String transacaoId, int quantidadeTotal, double valorTotal,
                     LocalDateTime agora) {
        this.pedidoId = pedidoId;
        this.transacaoId = transacaoId;
        this.quantidadeTotal = quantidadeTotal;
        this.valorTotal = valorTotal;
        this.status = StatusPagamento.PENDENTE;
        this.criadoEm = agora;
        this.atualizadoEm = agora;
    }

    public void aprovar(LocalDateTime agora) {
        if (valorTotal < VALOR_MINIMO) {
            throw new ValorMinimoNaoAtingidoException();
        }
        this.status = StatusPagamento.APROVADO;
        this.atualizadoEm = agora;
    }

    public void estornar(LocalDateTime agora) {
        this.status = StatusPagamento.ESTORNADO;
        this.atualizadoEm = agora;
    }
}
