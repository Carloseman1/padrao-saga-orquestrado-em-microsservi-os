package com.livraria.usuario.integracao.viacep;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class ViaCepResponseTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void le_a_resposta_real_ignorando_os_campos_que_nao_usamos() throws Exception {
        String json = """
                {"cep":"01001-000","logradouro":"Praca da Se","complemento":"lado impar",
                 "unidade":"","bairro":"Se","localidade":"Sao Paulo","uf":"SP",
                 "estado":"Sao Paulo","regiao":"Sudeste","ibge":"3550308","gia":"1004",
                 "ddd":"11","siafi":"7107"}
                """;

        ViaCepResponse resposta = mapper.readValue(json, ViaCepResponse.class);

        assertThat(resposta.logradouro()).isEqualTo("Praca da Se");
        assertThat(resposta.localidade()).isEqualTo("Sao Paulo");
        assertThat(resposta.uf()).isEqualTo("SP");
        assertThat(resposta.naoEncontrado()).isFalse();
        assertThat(resposta.temCidadeEEstado()).isTrue();
    }

    @Test
    void reconhece_cep_inexistente_com_erro_booleano() throws Exception {
        ViaCepResponse resposta = mapper.readValue("{\"erro\": true}", ViaCepResponse.class);

        assertThat(resposta.naoEncontrado()).isTrue();
    }

    @Test
    void reconhece_cep_inexistente_com_erro_em_texto() throws Exception {
        ViaCepResponse resposta = mapper.readValue("{\"erro\": \"true\"}", ViaCepResponse.class);

        assertThat(resposta.naoEncontrado()).isTrue();
    }

    @Test
    void cidade_pequena_vem_sem_logradouro_e_sem_bairro_e_continua_valida() throws Exception {
        String json = """
                {"cep":"65930-000","logradouro":"","complemento":"","unidade":"","bairro":"",
                 "localidade":"Acailandia","uf":"MA","estado":"Maranhao","regiao":"Nordeste"}
                """;

        ViaCepResponse resposta = mapper.readValue(json, ViaCepResponse.class);

        assertThat(resposta.logradouro()).isEmpty();
        assertThat(resposta.bairro()).isEmpty();
        assertThat(resposta.temCidadeEEstado()).isTrue();
    }

    @Test
    void resposta_sem_cidade_e_estado_nao_serve() throws Exception {
        ViaCepResponse resposta = mapper.readValue("{\"cep\":\"01001000\"}", ViaCepResponse.class);

        assertThat(resposta.temCidadeEEstado()).isFalse();
    }
}