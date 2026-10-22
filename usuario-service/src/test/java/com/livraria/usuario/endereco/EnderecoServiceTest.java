package com.livraria.usuario.endereco;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.livraria.usuario.endereco.exceptions.CepInvalidoException;
import com.livraria.usuario.endereco.exceptions.CepNaoEncontradoException;
import com.livraria.usuario.integracao.viacep.ViaCepClientTest;
import com.livraria.usuario.integracao.viacep.ViaCepResponse;
import com.livraria.usuario.integracao.viacep.exceptions.ViaCepRespostaInesperadaException;

@ExtendWith(MockitoExtension.class)
class EnderecoServiceTest {

    @Mock
    private ViaCepClientTest viaCepClient;

    private EnderecoService enderecoService;

    @BeforeEach
    void criarService() {
        enderecoService = new EnderecoService(viaCepClient);
    }

    @Test
    void cep_fora_do_formato_nao_chega_a_chamar_a_api() {
        assertThatThrownBy(() -> enderecoService.buscar("123"))
                .isInstanceOf(CepInvalidoException.class);

        verifyNoInteractions(viaCepClient);
    }

    @Test
    void cep_nulo_tambem_para_antes_da_chamada() {
        assertThatThrownBy(() -> enderecoService.buscar(null))
                .isInstanceOf(CepInvalidoException.class);

        verifyNoInteractions(viaCepClient);
    }

    @Test
    void consulta_com_o_cep_sem_mascara_e_devolve_o_endereco() {
        when(viaCepClient.consultar("01001000")).thenReturn(new ViaCepResponse(
                "01001-000", "Praca da Se", "Se", "Sao Paulo", "SP", null));

        Endereco endereco = enderecoService.buscar("01001-000");

        assertThat(endereco.cep()).isEqualTo("01001000");
        assertThat(endereco.rua()).isEqualTo("Praca da Se");
        assertThat(endereco.bairro()).isEqualTo("Se");
        assertThat(endereco.cidade()).isEqualTo("Sao Paulo");
        assertThat(endereco.estado()).isEqualTo("SP");
    }

    @Test
    void numero_e_complemento_ficam_vazios_porque_o_viacep_nao_sabe() {
        when(viaCepClient.consultar("01001000")).thenReturn(new ViaCepResponse(
                "01001-000", "Praca da Se", "Se", "Sao Paulo", "SP", null));

        Endereco endereco = enderecoService.buscar("01001000");

        assertThat(endereco.numero()).isNull();
        assertThat(endereco.complemento()).isNull();
    }

    @Test
    void cep_inexistente_vira_nao_encontrado() {
        when(viaCepClient.consultar("99999999")).thenReturn(new ViaCepResponse(
                null, null, null, null, null, "true"));

        assertThatThrownBy(() -> enderecoService.buscar("99999999"))
                .isInstanceOf(CepNaoEncontradoException.class);
    }

    @Test
    void corpo_vazio_vira_nao_encontrado() {
        when(viaCepClient.consultar("01001000")).thenReturn(null);

        assertThatThrownBy(() -> enderecoService.buscar("01001000"))
                .isInstanceOf(CepNaoEncontradoException.class);
    }

    @Test
    void resposta_sem_cidade_vira_formato_inesperado() {
        when(viaCepClient.consultar("01001000")).thenReturn(new ViaCepResponse(
                "01001-000", "Praca da Se", "Se", null, null, null));

        assertThatThrownBy(() -> enderecoService.buscar("01001000"))
                .isInstanceOf(ViaCepRespostaInesperadaException.class);
    }
}