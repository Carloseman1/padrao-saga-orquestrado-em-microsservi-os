package com.livraria.pedido.evento;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface EventoRepository extends MongoRepository<Evento, String> {

    List<Evento> findAllByPedidoUsuarioIdOrderByCriadoEmDesc(Long usuarioId);

    Optional<Evento> findTop1ByPedidoIdAndPedidoUsuarioIdOrderByCriadoEmDesc(String pedidoId, Long usuarioId);

    Optional<Evento> findTop1ByTransacaoIdAndPedidoUsuarioIdOrderByCriadoEmDesc(String transacaoId, Long usuarioId);
}
