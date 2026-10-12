package com.livraria.estoque.estoque;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EstoqueRepository extends JpaRepository<Estoque, Long> {

    Optional<Estoque> findByCodigoLivro(String codigoLivro);
}
