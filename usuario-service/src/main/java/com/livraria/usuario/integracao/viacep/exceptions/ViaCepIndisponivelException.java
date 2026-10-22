package com.livraria.usuario.integracao.viacep.exceptions;

import com.livraria.usuario.shared.exception.ErrorDTO;
import com.livraria.usuario.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class ViaCepIndisponivelException extends RuntimeException implements IErroPadrao {

    public ViaCepIndisponivelException(Throwable causa) {
        super(causa);
    }

    public ViaCepIndisponivelException() {
        super();
    }

    @Override
    public ErrorDTO toErrorDTO() {
        return new ErrorDTO(
                "CEP_INDISPONIVEL",
                "O serviço de consulta de CEP está indisponível neste momento.", HttpStatus.BAD_GATEWAY
        );
    }
}