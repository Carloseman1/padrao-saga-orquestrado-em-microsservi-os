package com.livraria.pagamento.saga.exceptions;

import com.livraria.pagamento.shared.exception.ErrorDTO;
import com.livraria.pagamento.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class JsonInvalidoException extends RuntimeException implements IErroPadrao {

    public JsonInvalidoException(Throwable causa) {
        super(causa);
    }

    @Override
    public ErrorDTO toErrorDTO() {
        return new ErrorDTO("JSON_INVALIDO", "Não foi possível converter o evento da saga.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
