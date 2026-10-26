package com.livraria.pagamento.cartao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.livraria.pagamento.cartao.exceptions.CvvInvalidoException;

class CvvTest {

    @Test
    void reconhece_par_e_impar() {
        assertThat(new Cvv("124").ehPar()).isTrue();
        assertThat(new Cvv("123").ehPar()).isFalse();
    }

    @Test
    void zero_e_par() {
        assertThat(new Cvv("000").ehPar()).isTrue();
    }

    @Test
    void aceita_tres_e_quatro_digitos() {
        assertThat(new Cvv("123").valor()).isEqualTo("123");
        assertThat(new Cvv("1235").valor()).isEqualTo("1235");
    }

    @Test
    void recusa_fora_do_formato() {
        assertThatThrownBy(() -> new Cvv("12")).isInstanceOf(CvvInvalidoException.class);
        assertThatThrownBy(() -> new Cvv("12345")).isInstanceOf(CvvInvalidoException.class);
        assertThatThrownBy(() -> new Cvv("abc")).isInstanceOf(CvvInvalidoException.class);
        assertThatThrownBy(() -> new Cvv(null)).isInstanceOf(CvvInvalidoException.class);
    }
}
