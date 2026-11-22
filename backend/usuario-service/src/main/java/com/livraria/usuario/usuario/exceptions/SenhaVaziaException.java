package com.livraria.usuario.usuario.exceptions;

import com.livraria.usuario.shared.exception.ErrorDTO;
import com.livraria.usuario.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class SenhaVaziaException extends RuntimeException implements IErroPadrao {
    @Override
    public ErrorDTO toErrorDTO(){
        return new ErrorDTO("SENHA_VAZIA","Informe uma senha.", HttpStatus.BAD_REQUEST);
    }
}
