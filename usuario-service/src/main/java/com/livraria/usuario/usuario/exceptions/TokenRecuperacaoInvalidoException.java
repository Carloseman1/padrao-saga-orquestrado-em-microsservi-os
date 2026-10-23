package com.livraria.usuario.usuario.exceptions;

import com.livraria.usuario.shared.exception.ErrorDTO;
import com.livraria.usuario.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class TokenRecuperacaoInvalidoException extends RuntimeException implements IErroPadrao {
    @Override
    public ErrorDTO toErrorDTO() {
        return new ErrorDTO("TOKEN_RECUPERACAO_INVALIDO",
                "O link de recuperação é inválido, já foi usado ou expirou. Solite outro.",
                HttpStatus.BAD_REQUEST);
    }
}
