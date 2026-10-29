package com.livraria.orquestrador.saga.exceptions;

import com.livraria.orquestrador.shared.exception.ErrorDTO;
import com.livraria.orquestrador.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class EnvioNaoConfirmadoException extends RuntimeException implements IErroPadrao {

    public EnvioNaoConfirmadoException(Throwable causa) {
        super(causa);
    }

    @Override
    public ErrorDTO toErrorDTO() {
        return new ErrorDTO("ENVIO_NAO_CONFIRMADO", "O Kafka não confirmou o envio do evento da saga.",
                HttpStatus.SERVICE_UNAVAILABLE);
    }
}
