package com.livraria.validacao.validacao;

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
@Table(name = "validacao")
@Data
@EqualsAndHashCode(of = "id")
public class Validacao {

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

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    protected Validacao() {
    }

    public Validacao(String pedidoId, String transacaoId, boolean sucesso, LocalDateTime agora) {
        this.pedidoId = pedidoId;
        this.transacaoId = transacaoId;
        this.sucesso = sucesso;
        this.criadoEm = agora;
        this.atualizadoEm = agora;
    }

    public void marcarFalha(LocalDateTime agora) {
        this.sucesso = false;
        this.atualizadoEm = agora;
    }
}
