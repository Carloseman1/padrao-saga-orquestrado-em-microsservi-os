package com.livraria.pedido.seguranca.exceptions;

import com.livraria.pedido.shared.exception.ErrorDTO;
import com.livraria.pedido.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class TokenInvalidoException  extends RuntimeException implements IErroPadrao {

    @Override
    public ErrorDTO toErrorDTO(){
        return new ErrorDTO("TOKEN_INVALIDO", "Token ausente ou inválido. Faça login novamente",
                HttpStatus.UNAUTHORIZED);
    }
}
