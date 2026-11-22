package com.livraria.estoque.estoque.exceptions;

import com.livraria.estoque.shared.exception.ErrorDTO;
import com.livraria.estoque.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class QuantidadeInvalidaException extends RuntimeException implements IErroPadrao {
    @Override
    public ErrorDTO toErrorDTO() {
        return new ErrorDTO("QUANTIDADE_INVALIDA", "A quantidade de entrada precisa ser maior que zero.", HttpStatus.BAD_REQUEST);
    }
}
