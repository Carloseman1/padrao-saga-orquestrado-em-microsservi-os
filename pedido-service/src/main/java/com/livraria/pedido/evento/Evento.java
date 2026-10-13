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

    public static final int VERSAO_ATUAL = 1;

    @Id
    private String id;
    private Integer versao;
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
        this.versao = VERSAO_ATUAL;
        this.pedidoId = pedido.getId();
        this.transacaoId = pedido.getTransacaoId();
        this.pedido = pedido;
        this.criadoEm = criadoEm;
    }

    public boolean versaoSuportada() {
        return versao != null && versao == VERSAO_ATUAL;
    }

    public void registrarFim(LocalDateTime agora) {
        this.pedidoId = pedido.getId();
        this.criadoEm = agora;
    }
}
