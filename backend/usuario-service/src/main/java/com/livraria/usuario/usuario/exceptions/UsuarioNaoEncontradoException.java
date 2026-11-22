package com.livraria.usuario.usuario.exceptions;

import com.livraria.usuario.shared.exception.ErrorDTO;
import com.livraria.usuario.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class UsuarioNaoEncontradoException extends RuntimeException implements IErroPadrao {

    @Override
    public ErrorDTO toErrorDTO(){
        return new ErrorDTO("USUARIO_NAO_ENCONTRADO", "Usuário não encontrado.",
                HttpStatus.NOT_FOUND);
    }
}
