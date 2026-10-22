package com.livraria.usuario.endereco.exceptions;

import com.livraria.usuario.shared.exception.ErrorDTO;
import com.livraria.usuario.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class CepInvalidoException extends RuntimeException implements IErroPadrao {
    @Override
    public ErrorDTO toErrorDTO() {
        return new ErrorDTO("CEP_INVALIDO", "O CEP deve ter 8 dígitos.", HttpStatus.BAD_REQUEST);
    }
}