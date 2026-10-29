package com.livraria.fiscal.sefaz;

import java.time.YearMonth;

public record ChaveDeAcesso(String valor) {

    private static final String MODELO_NFE = "55";
    private static final String EMISSAO_NORMAL = "1";

    public static ChaveDeAcesso gerar(int uf, YearMonth emissao, String cnpj, int serie, long numero,
                                      int codigoNumerico) {
        String semDigito = String.format("%02d%02d%02d%s%s%03d%09d%s%08d",
                uf, emissao.getYear() % 100, emissao.getMonthValue(), cnpj, MODELO_NFE, serie, numero,
                EMISSAO_NORMAL, codigoNumerico);
        return new ChaveDeAcesso(semDigito + digitoVerificador(semDigito));
    }

    static int digitoVerificador(String digitos) {
        int soma = 0;
        int peso = 2;

        for (int i = digitos.length() - 1; i >= 0; i--) {
            soma += Character.getNumericValue(digitos.charAt(i)) * peso;
            peso = peso == 9 ? 2 : peso + 1;
        }

        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
