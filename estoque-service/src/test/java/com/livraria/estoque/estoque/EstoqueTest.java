package com.livraria.estoque.estoque;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.livraria.estoque.estoque.exceptions.EstoqueInsuficienteException;
import com.livraria.estoque.estoque.exceptions.QuantidadeInvalidaException;

class EstoqueTest {

    @Test
    void repor_soma_ao_que_ja_tem() {
        Estoque estoque = new Estoque("DOM_CASMURRO", 2);

        estoque.repor(5);

        assertThat(estoque.getDisponivel()).isEqualTo(7);
    }

    @Test
    void recusa_reposicao_sem_quantidade() {
        Estoque estoque = new Estoque("DOM_CASMURRO", 2);

        assertThatThrownBy(() -> estoque.repor(0)).isInstanceOf(QuantidadeInvalidaException.class);
        assertThat(estoque.getDisponivel()).isEqualTo(2);
    }

    @Test
    void baixa_a_quantidade_pedida() {
        Estoque estoque = new Estoque("DOM_CASMURRO", 4);

        estoque.baixar(3);

        assertThat(estoque.getDisponivel()).isEqualTo(1);
    }

    @Test
    void permite_zerar_o_estoque() {
        Estoque estoque = new Estoque("DOM_CASMURRO", 4);

        estoque.baixar(4);

        assertThat(estoque.getDisponivel()).isZero();
    }

    @Test
    void recusa_baixa_maior_que_o_disponivel() {
        Estoque estoque = new Estoque("DOM_CASMURRO", 2);

        assertThatThrownBy(() -> estoque.baixar(3)).isInstanceOf(EstoqueInsuficienteException.class);
        assertThat(estoque.getDisponivel()).isEqualTo(2);
    }

    @Test
    void restaura_a_quantidade_anterior() {
        Estoque estoque = new Estoque("DOM_CASMURRO", 1);

        estoque.restaurar(4);

        assertThat(estoque.getDisponivel()).isEqualTo(4);
    }
}
