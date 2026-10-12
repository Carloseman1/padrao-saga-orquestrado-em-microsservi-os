package com.livraria.pagamento.pagamento.exceptions;

import com.livraria.pagamento.shared.exception.ErrorDTO;
import com.livraria.pagamento.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class PagamentoDuplicadoException extends RuntimeException implements IErroPadrao {
    @Override
    public ErrorDTO toErrorDTO() {
        return new ErrorDTO("PAGAMENTO_DUPLICADO", "Já existe um pagamento para esta transação.", HttpStatus.CONFLICT);
    }
}
