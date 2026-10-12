package com.livraria.validacao.validacao.exceptions;

import com.livraria.validacao.shared.exception.ErrorDTO;
import com.livraria.validacao.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class ValidacaoDuplicadaException extends RuntimeException implements IErroPadrao {
    @Override
    public ErrorDTO toErrorDTO() {
        return new ErrorDTO("VALIDACAO_DUPLICADA", "Já existe uma validação para esta transação.", HttpStatus.CONFLICT);
    }
}
