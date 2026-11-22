package com.livraria.pagamento.cartao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.YearMonth;

import org.junit.jupiter.api.Test;

import com.livraria.pagamento.cartao.exceptions.ValidadeInvalidaException;

class ValidadeTest {

    private static final YearMonth SETEMBRO_DE_2026 = YearMonth.of(2026, 9);

    @Test
    void mes_anterior_esta_vencido() {
        assertThat(new Validade(8, 2026).estaVencida(SETEMBRO_DE_2026)).isTrue();
    }

    @Test
    void mes_corrente_nao_esta_vencido_porque_vale_ate_o_ultimo_dia() {
        assertThat(new Validade(9, 2026).estaVencida(SETEMBRO_DE_2026)).isFalse();
    }

    @Test
    void mes_futuro_nao_esta_vencido() {
        assertThat(new Validade(1, 2027).estaVencida(SETEMBRO_DE_2026)).isFalse();
    }

    @Test
    void formata_com_dois_digitos_no_mes() {
        assertThat(new Validade(9, 2026).formatada()).isEqualTo("09/2026");
        assertThat(new Validade(12, 2028).formatada()).isEqualTo("12/2028");
    }

    @Test
    void recusa_mes_fora_da_faixa() {
        assertThatThrownBy(() -> new Validade(0, 2026)).isInstanceOf(ValidadeInvalidaException.class);
        assertThatThrownBy(() -> new Validade(13, 2026)).isInstanceOf(ValidadeInvalidaException.class);
    }

    @Test
    void recusa_ano_sem_quatro_digitos() {
        assertThatThrownBy(() -> new Validade(9, 26)).isInstanceOf(ValidadeInvalidaException.class);
    }
}
