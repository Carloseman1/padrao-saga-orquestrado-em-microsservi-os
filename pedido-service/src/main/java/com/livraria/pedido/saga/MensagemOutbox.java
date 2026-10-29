package com.livraria.pedido.saga;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Document(collection = "outbox")
@Data
@EqualsAndHashCode(of = "id")
public class MensagemOutbox {

    @Id
    private String id;

    private String topico;
    private String chave;
    private String payload;
    private LocalDateTime criadoEm;
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
