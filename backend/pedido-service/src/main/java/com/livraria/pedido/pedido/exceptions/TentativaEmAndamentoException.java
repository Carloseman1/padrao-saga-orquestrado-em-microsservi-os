package com.livraria.pedido.pedido.exceptions;

import com.livraria.pedido.shared.exception.ErrorDTO;
import com.livraria.pedido.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class TentativaEmAndamentoException extends RuntimeException implements IErroPadrao {

    @Override
    public ErrorDTO toErrorDTO() {
        return new ErrorDTO("TENTATIVA_EM_ANDAMENTO",
                "Outra tentativa de pagamento deste pedido já está em andamento.", HttpStatus.CONFLICT);
    }
}
