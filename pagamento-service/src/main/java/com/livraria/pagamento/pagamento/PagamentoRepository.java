package com.livraria.pagamento.pagamento;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    boolean existsByPedidoIdAndTransacaoId(String pedidoId, String transacaoId);

    Optional<Pagamento> findByPedidoIdAndTransacaoId(String pedidoId, String transacaoId);
}
