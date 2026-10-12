package com.livraria.estoque.estoque;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {

    boolean existsByPedidoIdAndTransacaoId(String pedidoId, String transacaoId);

    List<MovimentacaoEstoque> findByPedidoIdAndTransacaoId(String pedidoId, String transacaoId);
}
