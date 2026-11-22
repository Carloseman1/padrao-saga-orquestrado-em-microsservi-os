package com.livraria.estoque.estoque;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BaixaEstoqueRepository extends JpaRepository<BaixaEstoque, Long> {

    boolean existsByPedidoIdAndTransacaoId(String pedidoId, String transacaoId);
}
