package com.livraria.usuario.usuario.exceptions;

import com.livraria.usuario.shared.exception.ErrorDTO;
import com.livraria.usuario.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class EmailCadastradoException extends RuntimeException implements IErroPadrao {
    @Override
    public ErrorDTO toErrorDTO(){
        return new ErrorDTO("EMAIL_CADASTRADO", "Este e-mail já está cadastrado.",
                HttpStatus.BAD_REQUEST);
    }
}
