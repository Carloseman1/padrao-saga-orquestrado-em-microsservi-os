package com.livraria.validacao.validacao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ValidacaoRepository extends JpaRepository<Validacao, Long> {

    boolean existsByPedidoIdAndTransacaoId(String pedidoId, String transacaoId);

    Optional<Validacao> findByPedidoIdAndTransacaoId(String pedidoId, String transacaoId);
}
