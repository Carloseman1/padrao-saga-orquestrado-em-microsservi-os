package com.livraria.validacao.catalogo.exceptions;

import com.livraria.validacao.shared.exception.ErrorDTO;
import com.livraria.validacao.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class PrecoInvalidoException extends RuntimeException implements IErroPadrao {

    @Override
    public ErrorDTO toErrorDTO(){
        return new ErrorDTO("PRECO_INVALIDO","Informe um preço maior que zero.",
                HttpStatus.BAD_REQUEST);
    }
}
