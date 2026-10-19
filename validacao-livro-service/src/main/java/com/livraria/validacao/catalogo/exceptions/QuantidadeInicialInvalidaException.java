package com.livraria.validacao.catalogo.exceptions;

import com.livraria.validacao.shared.exception.ErrorDTO;
import com.livraria.validacao.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class QuantidadeInicialInvalidaException extends RuntimeException implements IErroPadrao {
    @Override
    public ErrorDTO toErrorDTO() {
        return new ErrorDTO("QUANTIDADE_INICIAL_INVALIDA", "A quantidade inicial não pode ser negativa.", HttpStatus.BAD_REQUEST);
    }
}
