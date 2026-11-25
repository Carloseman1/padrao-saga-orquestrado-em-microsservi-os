package com.livraria.usuario.endereco;

public record EnderecoInformado(String rua, String bairro, String cidade, String estado) {

    public boolean completo() {
        return preenchido(rua) && preenchido(bairro) && preenchido(cidade) && preenchido(estado);
    }

    private static boolean preenchido(String valor) {
        return valor != null && !valor.isBlank();
    }
}
