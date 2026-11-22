package com.livraria.usuario.usuario.dto;

import com.livraria.usuario.endereco.Endereco;
import com.livraria.usuario.usuario.Perfil;
import com.livraria.usuario.usuario.Usuario;

public record UsuarioDTO(
        Long id,
        String nome,
        String sobrenome,
        String email,
        Perfil perfil,
        Endereco endereco) {

    public static UsuarioDTO de(Usuario usuario) {
        return new UsuarioDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getSobrenome(),
                usuario.getEmail(),
                usuario.getPerfil(),
                usuario.getEndereco());
    }
}
