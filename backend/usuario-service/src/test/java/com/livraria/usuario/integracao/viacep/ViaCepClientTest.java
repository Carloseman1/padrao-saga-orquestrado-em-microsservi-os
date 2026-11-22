package com.livraria.usuario.integracao.viacep;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.SocketTimeoutException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.livraria.usuario.integracao.viacep.exceptions.ViaCepIndisponivelException;
import com.livraria.usuario.integracao.viacep.exceptions.ViaCepRespostaInesperadaException;
import com.livraria.usuario.integracao.viacep.exceptions.ViaCepTimeoutException;

class ViaCepClientTest {

    private static final String CEP = "01001000";
    private static final String URL = "https://viacep.com.br/ws/" + CEP + "/json/";

    private MockRestServiceServer viaCep;
    private ViaCepClient client;

    @BeforeEach
    void prepararClient() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://viacep.com.br/ws");
        viaCep = MockRestServiceServer.bindTo(builder).build();
        client = new ViaCepClient(builder.build());
    }

    @Test
    void le_o_endereco_quando_o_viacep_responde() {
        viaCep.expect(requestTo(URL)).andRespond(withSuccess("""
                {"cep":"01001-000","logradouro":"Praça da Sé","bairro":"Sé",
                 "localidade":"São Paulo","uf":"SP"}
                """, MediaType.APPLICATION_JSON));

        ViaCepResponse resposta = client.consultar(CEP);

        assertThat(resposta.localidade()).isEqualTo("São Paulo");
        assertThat(resposta.uf()).isEqualTo("SP");
        assertThat(resposta.naoEncontrado()).isFalse();
    }

    @Test
    void cep_inexistente_chega_como_200_com_erro_no_corpo() {
        viaCep.expect(requestTo(URL))
                .andRespond(withSuccess("{\"erro\":\"true\"}", MediaType.APPLICATION_JSON));

        assertThat(client.consultar(CEP).naoEncontrado()).isTrue();
    }

    @Test
    void timeout_vira_excecao_de_timeout() {
        viaCep.expect(requestTo(URL)).andRespond(requisicao -> {
            throw new SocketTimeoutException("Read timed out");
        });

        assertThatThrownBy(() -> client.consultar(CEP))
                .isInstanceOf(ViaCepTimeoutException.class);
    }

    @Test
    void erro_do_servidor_deles_vira_indisponivel() {
        viaCep.expect(requestTo(URL)).andRespond(withServerError());

        assertThatThrownBy(() -> client.consultar(CEP))
                .isInstanceOf(ViaCepIndisponivelException.class);
    }

    @Test
    void resposta_que_nao_esperamos_vira_resposta_inesperada() {
        viaCep.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> client.consultar(CEP))
                .isInstanceOf(ViaCepRespostaInesperadaException.class);
    }
}