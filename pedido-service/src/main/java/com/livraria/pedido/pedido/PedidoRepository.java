package com.livraria.pedido.pedido;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PedidoRepository extends MongoRepository<Pedido, String> {

    Optional<Pedido> findByIdAndUsuarioId(String id, Long usuarioId);
}
