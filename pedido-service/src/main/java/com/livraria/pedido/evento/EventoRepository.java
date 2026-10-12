package com.livraria.pedido.evento;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface EventoRepository extends MongoRepository<Evento, String> {

    List<Evento> findAllByOrderByCriadoEmDesc();

    Optional<Evento> findTop1ByPedidoIdOrderByCriadoEmDesc(String pedidoId);

    Optional<Evento> findTop1ByTransacaoIdOrderByCriadoEmDesc(String transacaoId);
}
