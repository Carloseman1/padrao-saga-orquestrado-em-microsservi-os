package com.livraria.usuario.usuario.dto;

import com.livraria.usuario.endereco.EnderecoInformado;

public record UsuarioRequest(
        String nome,
        String sobrenome,
        String email,
        String senha,
        String cep,
        String numero,
        String complemento,
        EnderecoInformado enderecoInformado
) {
}
