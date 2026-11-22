package com.livraria.usuario.integracao.viacep;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ViaCepResponse(
        String cep,
        String logradouro,
        String bairro,
        String localidade,
        String uf,
        String erro) {

    public boolean naoEncontrado(){
        return "true".equalsIgnoreCase(erro);
    }

    public boolean temCidadeEEstado(){
        return preenchido(localidade) && preenchido(uf);
    }

    private static boolean preenchido(String valor){
        return valor != null && !valor.isBlank();
    }
}
