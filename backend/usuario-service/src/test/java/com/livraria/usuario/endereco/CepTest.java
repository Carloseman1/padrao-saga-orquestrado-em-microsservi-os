package com.livraria.usuario.endereco;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.livraria.usuario.endereco.exceptions.CepInvalidoException;

class CepTest {

    @Test
    void guarda_apenas_os_digitos() {
        assertThat(new Cep("01001-000").valor()).isEqualTo("01001000");
        assertThat(new Cep(" 01001000 ").valor()).isEqualTo("01001000");
    }

    @Test
    void recusa_menos_de_oito_digitos() {
        assertThatThrownBy(() -> new Cep("1001000")).isInstanceOf(CepInvalidoException.class);
    }

    @Test
    void recusa_mais_de_oito_digitos() {
        assertThatThrownBy(() -> new Cep("010010000")).isInstanceOf(CepInvalidoException.class);
    }

    @Test
    void recusa_nulo_e_texto_sem_digito() {
        assertThatThrownBy(() -> new Cep(null)).isInstanceOf(CepInvalidoException.class);
        assertThatThrownBy(() -> new Cep("cep aqui")).isInstanceOf(CepInvalidoException.class);
    }
}