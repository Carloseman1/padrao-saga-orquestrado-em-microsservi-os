package com.livraria.pagamento.cartao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.livraria.pagamento.cartao.exceptions.NumeroCartaoInvalidoException;

class NumeroCartaoTest {

    @Test
    void guarda_apenas_os_digitos() {
        NumeroCartao numero = new NumeroCartao("4111 1111-1111.1111");

        assertThat(numero.valor()).isEqualTo("4111111111111111");
    }

    @Test
    void mascara_deixando_os_quatro_ultimos() {
        NumeroCartao numero = new NumeroCartao("5555 5555 5555 4444");

        assertThat(numero.ultimosQuatro()).isEqualTo("4444");
        assertThat(numero.mascarado()).isEqualTo("**** **** **** 4444");
    }

    @Test
    void recusa_numero_curto() {
        assertThatThrownBy(() -> new NumeroCartao("411111111111"))
                .isInstanceOf(NumeroCartaoInvalidoException.class);
    }

    @Test
    void recusa_numero_que_falha_no_luhn() {
        assertThatThrownBy(() -> new NumeroCartao("4111111111111112"))
                .isInstanceOf(NumeroCartaoInvalidoException.class);
    }

    @Test
    void recusa_nulo_e_texto_sem_digito() {
        assertThatThrownBy(() -> new NumeroCartao(null))
                .isInstanceOf(NumeroCartaoInvalidoException.class);

        assertThatThrownBy(() -> new NumeroCartao("sem digito"))
                .isInstanceOf(NumeroCartaoInvalidoException.class);
    }
}
