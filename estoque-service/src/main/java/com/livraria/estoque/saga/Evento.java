package com.livraria.estoque.saga;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class Evento {

    private String id;
    private String pedidoId;
    private String transacaoId;
    private Pedido pedido;
    private String origem;
    private StatusSaga status;
    private List<Historico> historico;
    private LocalDateTime criadoEm;

    public void registrar(String origem, StatusSaga status, String mensagem, LocalDateTime agora) {
        this.origem = origem;
        this.status = status;
        if (historico == null) {
            historico = new ArrayList<>();
        }
        historico.add(new Historico(origem, status, mensagem, agora));
    }
}
