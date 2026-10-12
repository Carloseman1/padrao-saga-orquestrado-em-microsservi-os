package com.livraria.pedido.evento.exceptions;

import com.livraria.pedido.shared.exception.ErrorDTO;
import com.livraria.pedido.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class FiltroNaoInformadoException extends RuntimeException implements IErroPadrao {
    @Override
    public ErrorDTO toErrorDTO() {
        return new ErrorDTO("FILTRO_NAO_INFORMADO", "Informe o pedidoId ou o transacaoId.", HttpStatus.BAD_REQUEST);
    }
}
