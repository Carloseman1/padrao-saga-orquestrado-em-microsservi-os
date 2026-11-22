package com.livraria.estoque.estoque;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {

    List<MovimentacaoEstoque> findByPedidoIdAndTransacaoId(String pedidoId, String transacaoId);
}
