package com.livraria.orquestrador.orquestracao.exceptions;

import com.livraria.orquestrador.shared.exception.ErrorDTO;
import com.livraria.orquestrador.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class OrigemOuStatusNaoInformadoException extends RuntimeException implements IErroPadrao {
    @Override
    public ErrorDTO toErrorDTO() {
        return new ErrorDTO("ORIGEM_OU_STATUS_NAO_INFORMADO", "Informe a origem e o status do evento.", HttpStatus.BAD_REQUEST);
    }
}
