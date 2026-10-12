package com.livraria.pedido.evento;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.livraria.pedido.pedido.Pedido;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Document(collection = "evento")
@Data
@EqualsAndHashCode(of = "id")
public class Evento {

    @Id
    private String id;
    private String transacaoId;
    private String pedidoId;
    private Pedido pedido;
    private String origem;
    private String status;
    private List<Historico> historico;
    private LocalDateTime criadoEm;

    protected Evento() {
    }

    public Evento(Pedido pedido, LocalDateTime criadoEm) {
        this.pedidoId = pedido.getId();
        this.transacaoId = pedido.getTransacaoId();
        this.pedido = pedido;
        this.criadoEm = criadoEm;
    }

    public void registrarFim(LocalDateTime agora) {
        this.pedidoId = pedido.getId();
        this.criadoEm = agora;
    }
}
