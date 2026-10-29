package com.livraria.fiscal.saga;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxRepository extends JpaRepository<MensagemOutbox, Long> {

    List<MensagemOutbox> findTop100ByEnviadoEmIsNullOrderByIdAsc();
}
