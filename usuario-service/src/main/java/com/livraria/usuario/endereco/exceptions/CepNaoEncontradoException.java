package com.livraria.usuario.endereco.exceptions;

import com.livraria.usuario.shared.exception.ErrorDTO;
import com.livraria.usuario.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class CepNaoEncontradoException extends RuntimeException implements IErroPadrao {
    @Override
    public ErrorDTO toErrorDTO(){
        return new ErrorDTO("CEP_NÃO_ENCONTRADO","Nenhum endereço foi encontrado para o CEP informado.",
                HttpStatus.NOT_FOUND);
    }
}
