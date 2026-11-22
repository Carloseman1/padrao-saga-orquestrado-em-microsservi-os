package com.livraria.orquestrador.saga;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.livraria.orquestrador.saga.exceptions.JsonInvalidoException;
import com.livraria.orquestrador.saga.exceptions.VersaoNaoSuportadaException;

class ContratoDoEventoTest {

    private final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json()
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();
    private final ConversorJson conversorJson = new ConversorJson(objectMapper);

    @Test
    void repassa_o_evento_do_contrato_sem_perder_nenhum_campo() throws IOException {
        String contrato = lerContrato();

        Evento evento = conversorJson.paraEvento(contrato);

        assertThat(arvore(conversorJson.paraJson(evento))).isEqualTo(arvore(contrato));
    }

    @Test
    void preserva_campos_que_este_servico_nao_conhece() throws IOException {
        Evento evento = conversorJson.paraEvento(lerContrato());

        JsonNode reenviado = arvore(conversorJson.paraJson(evento));

        assertThat(reenviado.path("campoNovoNoEvento").asText()).isEqualTo("precisa chegar ao próximo serviço");
        assertThat(reenviado.path("pedido").path("campoNovoNoPedido").asText())
                .isEqualTo("precisa chegar ao próximo serviço");
    }

    @Test
    void recusa_versao_que_nao_conhece() throws IOException {
        String versaoFutura = lerContrato().replace("\"versao\": 1", "\"versao\": 2");

        assertThatThrownBy(() -> conversorJson.paraEvento(versaoFutura))
                .isInstanceOf(VersaoNaoSuportadaException.class);
    }

    @Test
    void recusa_evento_sem_versao() throws IOException {
        String semVersao = lerContrato().replace("\"versao\": 1,", "");

        assertThatThrownBy(() -> conversorJson.paraEvento(semVersao))
                .isInstanceOf(VersaoNaoSuportadaException.class);
    }

    @Test
    void recusa_json_quebrado() {
        assertThatThrownBy(() -> conversorJson.paraEvento("{ isto não é json"))
                .isInstanceOf(JsonInvalidoException.class);
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
