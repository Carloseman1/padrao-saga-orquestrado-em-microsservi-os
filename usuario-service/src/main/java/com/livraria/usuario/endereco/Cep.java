package com.livraria.usuario.endereco;

import com.livraria.usuario.endereco.exceptions.CepInvalidoException;

public record Cep(String valor) {

    private static final  int DIGITOS = 8;

    public Cep{
        valor = valor == null ? "" : valor.replaceAll("\\D","");

        if(valor.length() != DIGITOS){
            throw new CepInvalidoException();
        }
    }
}
