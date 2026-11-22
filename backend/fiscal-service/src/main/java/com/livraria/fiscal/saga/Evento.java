package com.livraria.fiscal.saga;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class Evento {

    public static final int VERSAO_ATUAL = 1;

    private Integer versao;
    private String id;
    private String pedidoId;
    private String transacaoId;
    private Pedido pedido;
    private String origem;
    private StatusSaga status;
    private List<Historico> historico;
    private LocalDateTime criadoEm;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Map<String, Object> camposDesconhecidos = new LinkedHashMap<>();

    public boolean versaoSuportada() {
        return versao != null && versao == VERSAO_ATUAL;
    }

    public void registrar(String origem, StatusSaga status, String mensagem, LocalDateTime agora) {
        this.origem = origem;
        this.status = status;
        if (historico == null) {
            historico = new ArrayList<>();
        }
        historico.add(new Historico(origem, status, mensagem, agora));
    }

    @JsonAnySetter
    public void guardarCampoDesconhecido(String nome, Object valor) {
        camposDesconhecidos.put(nome, valor);
    }

    @JsonAnyGetter
    public Map<String, Object> camposDesconhecidos() {
        return camposDesconhecidos;
    }
}
