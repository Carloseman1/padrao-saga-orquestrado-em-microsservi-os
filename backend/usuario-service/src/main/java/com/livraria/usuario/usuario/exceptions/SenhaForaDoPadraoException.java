package com.livraria.usuario.usuario.exceptions;

import com.livraria.usuario.shared.exception.ErrorDTO;
import com.livraria.usuario.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class SenhaForaDoPadraoException extends RuntimeException implements IErroPadrao {
    @Override
    public ErrorDTO toErrorDTO(){
        return new ErrorDTO("SENHA_FORA_DO_PADRAO", "A Senha deve ter ao menos 8 caracteres, uma maiúscula, uma minúscula," +
                " um número e um caractere especial.", HttpStatus.BAD_REQUEST);
    }
}
