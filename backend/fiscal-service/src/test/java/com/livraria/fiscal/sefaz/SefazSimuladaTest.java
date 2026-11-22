package com.livraria.fiscal.sefaz;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

class SefazSimuladaTest {

    private final SefazSimulada sefaz = new SefazSimulada(List.of("O_GUARANI"));

    @Test
    void autoriza_nota_com_livros_validos() {
        RespostaSefaz resposta = sefaz.autorizar(solicitacao("DOM_CASMURRO"));

        assertThat(resposta.aceita()).isTrue();
        assertThat(resposta.codigo()).isEqualTo("100");
        assertThat(resposta.protocolo()).hasSize(15).startsWith("135");
    }

    @Test
    void rejeita_nota_com_livro_de_ncm_invalido() {
        RespostaSefaz resposta = sefaz.autorizar(solicitacao("DOM_CASMURRO", "O_GUARANI"));

        assertThat(resposta.aceita()).isFalse();
        assertThat(resposta.codigo()).isEqualTo("778");
        assertThat(resposta.protocolo()).isNull();
    }

    @Test
    void registra_o_cancelamento() {
        RespostaSefaz resposta = sefaz.cancelar("35261011222333000181550010000000011123456780", "teste");

        assertThat(resposta.aceita()).isTrue();
        assertThat(resposta.codigo()).isEqualTo("135");
    }

    private SolicitacaoAutorizacao solicitacao(String... codigos) {
        return new SolicitacaoAutorizacao("35261011222333000181550010000000011123456780",
                new BigDecimal("39.90"), List.of(codigos));
    }
}
