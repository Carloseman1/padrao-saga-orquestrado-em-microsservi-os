package com.livraria.estoque.estoque;

public enum SituacaoEstoque {
    DISPONIVEL,
    ULTIMAS_UNIDADES,
    ESGOTADO;

    private static final int LIMITE_DE_ULTIMAS_UNIDADES = 3;

    public static SituacaoEstoque de(int disponivel) {
        if (disponivel <= 0) {
            return ESGOTADO;
        }

        if (disponivel <= LIMITE_DE_ULTIMAS_UNIDADES) {
            return ULTIMAS_UNIDADES;
        }

        return DISPONIVEL;
    }
}
