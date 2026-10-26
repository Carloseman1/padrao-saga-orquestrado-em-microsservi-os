package com.livraria.pagamento.cartao;

import java.time.YearMonth;

import com.livraria.pagamento.cartao.exceptions.ValidadeInvalidaException;

public record Validade(int mes, int ano) {

    private static final int ANO_MINIMO = 2000;
    private static final int ANO_MAXIMO = 2100;

    public Validade {
        if (mes < 1 || mes > 12 || ano < ANO_MINIMO || ano > ANO_MAXIMO) {
            throw new ValidadeInvalidaException();
        }
    }

    public boolean estaVencida(YearMonth hoje) {
        return YearMonth.of(ano, mes).isBefore(hoje);
    }

    public String formatada() {
        return String.format("%02d/%d", mes, ano);
    }
}
