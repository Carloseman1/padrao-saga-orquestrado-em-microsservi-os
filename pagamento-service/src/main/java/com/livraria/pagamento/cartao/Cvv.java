package com.livraria.pagamento.cartao;

import com.livraria.pagamento.cartao.exceptions.CvvInvalidoException;

public record Cvv(String valor) {

    public Cvv {
        valor = valor == null ? "" : valor.trim();

        if (!valor.matches("\\d{3,4}")) {
            throw new CvvInvalidoException();
        }
    }

    public boolean ehPar() {
        return Integer.parseInt(valor) % 2 == 0;
    }
}
