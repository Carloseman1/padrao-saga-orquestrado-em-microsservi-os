package com.livraria.usuario.usuario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.livraria.usuario.usuario.exceptions.EmailInvalidoException;

class EmailTest {

    @Test
    void normaliza_para_minusculo_sem_espaco() {
        assertThat(new Email("  Usuario@Teste.COM ").valor())
                .isEqualTo("usuario@teste.com");
    }

    @Test
    void recusa_formato_invalido() {
        assertThatThrownBy(() -> new Email("usuario")).isInstanceOf(EmailInvalidoException.class);
        assertThatThrownBy(() -> new Email("usuario@")).isInstanceOf(EmailInvalidoException.class);
        assertThatThrownBy(() -> new Email("usuario@teste")).isInstanceOf(EmailInvalidoException.class);
        assertThatThrownBy(() -> new Email("usuario teste@teste.com"))
                .isInstanceOf(EmailInvalidoException.class);
    }

    @Test
    void recusa_email_longo_demais() {
        String local = "a".repeat(243);

        assertThatThrownBy(() -> new Email(local + "@example.com"))
                .isInstanceOf(EmailInvalidoException.class);
    }

    @Test
    void recusa_nulo_e_vazio() {
        assertThatThrownBy(() -> new Email(null)).isInstanceOf(EmailInvalidoException.class);
        assertThatThrownBy(() -> new Email("   ")).isInstanceOf(EmailInvalidoException.class);
    }
}