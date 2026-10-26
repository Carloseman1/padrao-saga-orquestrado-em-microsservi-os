package com.livraria.pagamento.saga;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
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
public class Pedido {

    private String id;
    private List<ItemPedido> itens;
    private Long usuarioId;
    private Long cartaoId;
    private String transacaoId;
    private BigDecimal valorTotal;
    private int quantidadeTotal;
    private LocalDateTime criadoEm;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Map<String, Object> camposDesconhecidos = new LinkedHashMap<>();

    public BigDecimal calcularValorTotal() {
        return itens.stream()
                .map(item -> item.livro().valorUnitario().multiply(BigDecimal.valueOf(item.quantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_EVEN);
    }

    public int calcularQuantidadeTotal() {
        return itens.stream()
                .mapToInt(ItemPedido::quantidade)
                .sum();
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
