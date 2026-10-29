package com.livraria.fiscal.sefaz;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.YearMonth;

import org.junit.jupiter.api.Test;

class ChaveDeAcessoTest {

    @Test
    void monta_os_44_digitos_com_o_digito_verificador_do_modulo_11() {
        ChaveDeAcesso chave = ChaveDeAcesso.gerar(35, YearMonth.of(2026, 10), "11222333000181", 1, 1, 12345678);

        assertThat(chave.valor()).isEqualTo("35261011222333000181550010000000011123456780");
        assertThat(chave.valor()).hasSize(44);
    }

    @Test
    void resto_maior_que_um_vira_onze_menos_o_resto() {
        assertThat(ChaveDeAcesso.digitoVerificador("1")).isEqualTo(9);
    }
}
