package com.livraria.usuario.integracao.viacep.exceptions;

import com.livraria.usuario.shared.exception.ErrorDTO;
import com.livraria.usuario.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class ViaCepTimeoutException extends RuntimeException implements IErroPadrao {

    public ViaCepTimeoutException(Throwable causa){
        super(causa);
    }

    public ViaCepTimeoutException(){
        super();
    }

    public ErrorDTO toErrorDTO(){
        return new ErrorDTO("CEP_TIMEOUT",
                "A consulta de CEP não respondeu no tempo limite. Tente novamente.",
                HttpStatus.GATEWAY_TIMEOUT);
    }
}
