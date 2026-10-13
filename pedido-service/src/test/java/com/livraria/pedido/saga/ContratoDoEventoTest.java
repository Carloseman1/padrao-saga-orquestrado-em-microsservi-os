package com.livraria.pedido.saga;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.livraria.pedido.evento.Evento;
import com.livraria.pedido.pedido.ItemPedido;
import com.livraria.pedido.pedido.Livro;
import com.livraria.pedido.pedido.Pedido;
import com.livraria.pedido.saga.exceptions.VersaoNaoSuportadaException;

class ContratoDoEventoTest {

    private final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json()
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();
    private final ConversorJson conversorJson = new ConversorJson(objectMapper);

    @Test
    void le_o_fim_da_saga_com_todos_os_campos_do_contrato() throws IOException {
        Evento evento = conversorJson.paraEvento(lerContrato());

        assertThat(arvore(conversorJson.paraJson(evento))).isEqualTo(contratoSemCamposNovos());
    }

    @Test
    void evento_criado_aqui_sai_com_a_versao_atual() throws IOException {
        Pedido pedido = new Pedido(
                List.of(new ItemPedido(new Livro("DOM_CASMURRO", new BigDecimal("39.90")), 1)),
                "transacao-1",
                LocalDateTime.of(2026, 10, 13, 10, 0, 5));

        JsonNode enviado = arvore(conversorJson.paraJson(new Evento(pedido, LocalDateTime.of(2026, 10, 13, 10, 0, 5))));

        assertThat(enviado.path("versao").asInt()).isEqualTo(Evento.VERSAO_ATUAL);
    }

    @Test
    void recusa_versao_que_nao_conhece() throws IOException {
        String versaoFutura = lerContrato().replace("\"versao\": 1", "\"versao\": 2");

        assertThatThrownBy(() -> conversorJson.paraEvento(versaoFutura))
                .isInstanceOf(VersaoNaoSuportadaException.class);
    }

    private JsonNode contratoSemCamposNovos() throws IOException {
        ObjectNode contrato = (ObjectNode) arvore(lerContrato());
        contrato.remove("campoNovoNoEvento");
        ((ObjectNode) contrato.get("pedido")).remove("campoNovoNoPedido");
        return contrato;
    }

    private String lerContrato() throws IOException {
        try (InputStream arquivo = getClass().getResourceAsStream("/contrato/evento-v1.json")) {
            return new String(arquivo.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private JsonNode arvore(String json) throws IOException {
        return objectMapper.readTree(json);
    }
}
