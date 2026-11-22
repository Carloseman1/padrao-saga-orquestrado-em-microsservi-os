ALTER TABLE validacao
    ADD CONSTRAINT uk_validacao_pedido_transacao UNIQUE (pedido_id, transacao_id);
