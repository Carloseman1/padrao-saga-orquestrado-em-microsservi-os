package com.livraria.usuario.usuario.exceptions;

import com.livraria.usuario.shared.exception.ErrorDTO;
import com.livraria.usuario.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class EmailInvalidoException extends RuntimeException implements IErroPadrao {
    @Override
    public ErrorDTO toErrorDTO(){
        return new ErrorDTO("EMAIL_INVALIDO", "informe um e-mail válido.",
                HttpStatus.BAD_REQUEST);
    }
}
