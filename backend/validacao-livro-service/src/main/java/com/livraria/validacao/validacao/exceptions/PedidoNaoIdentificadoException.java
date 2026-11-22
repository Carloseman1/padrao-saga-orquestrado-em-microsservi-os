package com.livraria.validacao.validacao.exceptions;

import com.livraria.validacao.shared.exception.ErrorDTO;
import com.livraria.validacao.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class PedidoNaoIdentificadoException extends RuntimeException implements IErroPadrao {
    @Override
    public ErrorDTO toErrorDTO() {
        return new ErrorDTO("PEDIDO_NAO_IDENTIFICADO", "Informe o pedido e a transação.", HttpStatus.BAD_REQUEST);
    }
}
