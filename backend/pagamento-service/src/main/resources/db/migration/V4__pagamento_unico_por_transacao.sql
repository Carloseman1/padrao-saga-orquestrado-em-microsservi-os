ALTER TABLE pagamento
    ADD CONSTRAINT uk_pagamento_pedido_transacao UNIQUE (pedido_id, transacao_id);
