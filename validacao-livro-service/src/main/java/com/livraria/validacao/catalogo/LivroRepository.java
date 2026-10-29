package com.livraria.validacao.catalogo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LivroRepository extends JpaRepository<Livro, Long> {

    boolean existsByCodigo(String codigo);

    Optional<Livro> findByCodigo(String codigo);
}
