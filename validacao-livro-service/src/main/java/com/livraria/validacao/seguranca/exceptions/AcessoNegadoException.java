package com.livraria.validacao.seguranca.exceptions;

import com.livraria.validacao.shared.exception.ErrorDTO;
import com.livraria.validacao.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class AcessoNegadoException extends RuntimeException implements IErroPadrao {

    @Override
    public ErrorDTO toErrorDTO(){
        return new ErrorDTO("ACESSO_NEGADO","Você não tem permissão para esta operação.",
                HttpStatus.FORBIDDEN);
    }
}
