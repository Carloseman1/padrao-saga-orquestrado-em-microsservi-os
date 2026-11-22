package com.livraria.usuario.usuario.dto;

public record UsuarioRequest(
        String nome,
        String sobrenome,
        String email,
        String senha,
        String cep,
        String numero,
        String complemento
) {
}
