package com.livraria.fiscal.fiscal;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NotaFiscalRepository extends JpaRepository<NotaFiscal, Long> {

    boolean existsByPedidoIdAndTransacaoId(String pedidoId, String transacaoId);

    Optional<NotaFiscal> findByPedidoIdAndTransacaoId(String pedidoId, String transacaoId);

    @Query(value = "SELECT nextval('numero_nota_fiscal')", nativeQuery = true)
    long proximoNumero();
}
