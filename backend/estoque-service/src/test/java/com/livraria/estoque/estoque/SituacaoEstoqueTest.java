package com.livraria.estoque.estoque;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SituacaoEstoqueTest {

    @Test
    void sem_unidades_esta_esgotado() {
        assertThat(SituacaoEstoque.de(0)).isEqualTo(SituacaoEstoque.ESGOTADO);
    }

    @Test
    void ate_tres_unidades_sao_as_ultimas() {
        assertThat(SituacaoEstoque.de(1)).isEqualTo(SituacaoEstoque.ULTIMAS_UNIDADES);
        assertThat(SituacaoEstoque.de(3)).isEqualTo(SituacaoEstoque.ULTIMAS_UNIDADES);
    }

    @Test
    void acima_de_tres_unidades_esta_disponivel() {
        assertThat(SituacaoEstoque.de(4)).isEqualTo(SituacaoEstoque.DISPONIVEL);
    }
}
