package com.livraria.pagamento.saga;

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
@Table(name = "outbox")
@Data
@EqualsAndHashCode(of = "id")
public class MensagemOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "topico", nullable = false)
    private String topico;

    @Column(name = "chave", nullable = false)
    private String chave;

    @Column(name = "payload", nullable = false, columnDefinition = "text")
    private String payload;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "enviado_em")
    private LocalDateTime enviadoEm;

    protected MensagemOutbox() {
    }

    public MensagemOutbox(String topico, String chave, String payload, LocalDateTime criadoEm) {
        this.topico = topico;
        this.chave = chave;
        this.payload = payload;
        this.criadoEm = criadoEm;
    }

    public void marcarEnviada(LocalDateTime agora) {
        this.enviadoEm = agora;
    }
}
