package com.livraria.pedido.pedido.exceptions;

import com.livraria.pedido.shared.exception.ErrorDTO;
import com.livraria.pedido.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class PedidoSemItensException extends RuntimeException implements IErroPadrao {
    @Override
    public ErrorDTO toErrorDTO() {
        return new ErrorDTO("PEDIDO_SEM_ITENS", "Informe ao menos um livro no pedido.", HttpStatus.BAD_REQUEST);
    }
}
