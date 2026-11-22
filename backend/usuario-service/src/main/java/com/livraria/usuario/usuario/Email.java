package com.livraria.usuario.usuario;

import com.livraria.usuario.usuario.exceptions.EmailInvalidoException;

import java.util.Locale;
import java.util.regex.Pattern;

public record Email(String valor) {

    private static final Pattern PADRAO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private static final int MAXIMO_DE_CARACTERES = 254;

    public Email {
        valor = valor == null ? "" : valor.trim().toLowerCase(Locale.ROOT);

        if(valor.length() > MAXIMO_DE_CARACTERES || !PADRAO.matcher(valor).matches()){
            throw new EmailInvalidoException();
        }
    }
}
