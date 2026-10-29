package com.livraria.pedido.saga;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface OutboxRepository extends MongoRepository<MensagemOutbox, String> {

    List<MensagemOutbox> findTop100ByEnviadoEmIsNullOrderByCriadoEmAscIdAsc();
}
