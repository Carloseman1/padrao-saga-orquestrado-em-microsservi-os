package com.livraria.fiscal.sefaz;

public record RespostaSefaz(boolean aceita, String codigo, String motivo, String protocolo) {

    public static RespostaSefaz aceita(String codigo, String motivo, String protocolo) {
        return new RespostaSefaz(true, codigo, motivo, protocolo);
    }

    public static RespostaSefaz recusada(String codigo, String motivo) {
        return new RespostaSefaz(false, codigo, motivo, null);
    }
}
