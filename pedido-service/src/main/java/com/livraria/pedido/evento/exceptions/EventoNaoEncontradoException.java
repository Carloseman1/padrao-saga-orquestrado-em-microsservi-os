package com.livraria.pedido.evento.exceptions;

import com.livraria.pedido.shared.exception.ErrorDTO;
import com.livraria.pedido.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class EventoNaoEncontradoException extends RuntimeException implements IErroPadrao {
    @Override
    public ErrorDTO toErrorDTO() {
        return new ErrorDTO("EVENTO_NAO_ENCONTRADO", "Evento não encontrado.", HttpStatus.NOT_FOUND);
    }
}
