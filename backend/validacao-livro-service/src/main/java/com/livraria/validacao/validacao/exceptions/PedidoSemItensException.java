package com.livraria.validacao.validacao.exceptions;

import com.livraria.validacao.shared.exception.ErrorDTO;
import com.livraria.validacao.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class PedidoSemItensException extends RuntimeException implements IErroPadrao {
    @Override
    public ErrorDTO toErrorDTO() {
        return new ErrorDTO("PEDIDO_SEM_ITENS", "O pedido não possui livros.", HttpStatus.BAD_REQUEST);
    }
}
