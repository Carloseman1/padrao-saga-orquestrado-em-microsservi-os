package com.livraria.usuario.usuario;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecuperacaoSenhaRepository extends JpaRepository<RecuperacaoSenha, Long> {

    Optional<RecuperacaoSenha> findByToken(String token);

    void  deleteByUsuarioIdAndUsadoEmIsNull(Long usuarioId);
}
