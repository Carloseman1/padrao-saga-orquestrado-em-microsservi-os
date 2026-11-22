package com.livraria.usuario.endereco;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Endereco(
        String cep,
        String rua,
        String bairro,
        String cidade,
        String estado,
        String numero,
        String complemento) {

    public static Endereco doCep(String cep, String rua, String bairro, String cidade, String estado) {
        return new Endereco(cep, rua, bairro, cidade, estado, null, null);
    }

    public Endereco completadoCom(String numero, String complemento) {
        return new Endereco(cep, rua, bairro, cidade, estado, numero, complemento);
    }
}