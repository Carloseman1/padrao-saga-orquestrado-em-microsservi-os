package com.livraria.fiscal.fiscal;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
@Table(name = "nota_fiscal")
@Data
@EqualsAndHashCode(of = "id")
public class NotaFiscal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pedido_id", nullable = false)
    private String pedidoId;

    @Column(name = "transacao_id", nullable = false)
    private String transacaoId;

    @Column(name = "numero", nullable = false)
    private long numero;

    @Column(name = "serie", nullable = false)
    private int serie;

    @Column(name = "chave_acesso", nullable = false, unique = true)
    private String chaveAcesso;

    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusNotaFiscal status;

    @Column(name = "protocolo")
    private String protocolo;

    @Column(name = "codigo_retorno")
    private String codigoRetorno;

    @Column(name = "motivo")
    private String motivo;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    protected NotaFiscal() {
    }

    public NotaFiscal(String pedidoId, String transacaoId, long numero, int serie, String chaveAcesso,
                      BigDecimal valorTotal, LocalDateTime agora) {
        this.pedidoId = pedidoId;
        this.transacaoId = transacaoId;
        this.numero = numero;
        this.serie = serie;
        this.chaveAcesso = chaveAcesso;
        this.valorTotal = valorTotal;
        this.status = StatusNotaFiscal.PENDENTE;
        this.criadoEm = agora;
        this.atualizadoEm = agora;
    }

    public void autorizar(String protocolo, LocalDateTime agora) {
        this.status = StatusNotaFiscal.AUTORIZADA;
        this.protocolo = protocolo;
        this.atualizadoEm = agora;
    }

    public void rejeitar(String codigoRetorno, String motivo, LocalDateTime agora) {
        this.status = StatusNotaFiscal.REJEITADA;
        this.codigoRetorno = codigoRetorno;
        this.motivo = motivo;
        this.atualizadoEm = agora;
    }

    public void cancelar(String protocolo, LocalDateTime agora) {
        this.status = StatusNotaFiscal.CANCELADA;
        this.protocolo = protocolo;
        this.atualizadoEm = agora;
    }

    public boolean foiAutorizada() {
        return status == StatusNotaFiscal.AUTORIZADA;
    }

    public boolean foiCancelada() {
        return status == StatusNotaFiscal.CANCELADA;
    }
}
