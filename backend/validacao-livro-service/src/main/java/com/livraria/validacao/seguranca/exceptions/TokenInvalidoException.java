package com.livraria.validacao.seguranca.exceptions;

import com.livraria.validacao.shared.exception.ErrorDTO;
import com.livraria.validacao.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class TokenInvalidoException  extends RuntimeException implements IErroPadrao {

    @Override
    public ErrorDTO toErrorDTO(){
        return new ErrorDTO("TOKEN_INVALIDO", "Token ausente ou inválido. Faça login novamente",
                HttpStatus.UNAUTHORIZED);
    }
}
