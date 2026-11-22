package com.livraria.pagamento.cartao;

import com.livraria.pagamento.cartao.exceptions.NumeroCartaoInvalidoException;

public record NumeroCartao(String valor) {

    private static final int MINIMO_DE_DIGITOS = 13;
    private static final int MAXIMO_DE_DIGITOS = 19;

    public NumeroCartao {
        valor = valor == null ? "" : valor.replaceAll("\\D", "");

        if (valor.length() < MINIMO_DE_DIGITOS || valor.length() > MAXIMO_DE_DIGITOS
                || !passaNoLuhn(valor)) {
            throw new NumeroCartaoInvalidoException();
        }
    }

    public static String mascarar(String ultimosQuatro) {
        return "**** **** **** " + ultimosQuatro;
    }

    public String ultimosQuatro() {
        return valor.substring(valor.length() - 4);
    }

    public String mascarado() {
        return mascarar(ultimosQuatro());
    }

    private static boolean passaNoLuhn(String digitos) {
        int soma = 0;
        boolean dobra = false;

        for (int i = digitos.length() - 1; i >= 0; i--) {
            int digito = digitos.charAt(i) - '0';

            if (dobra) {
                digito *= 2;
                if (digito > 9) {
                    digito -= 9;
                }
            }

            soma += digito;
            dobra = !dobra;
        }

        return soma % 10 == 0;
    }
}
