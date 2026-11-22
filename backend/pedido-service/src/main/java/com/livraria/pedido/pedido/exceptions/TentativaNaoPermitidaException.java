package com.livraria.pedido.pedido.exceptions;

import com.livraria.pedido.shared.exception.ErrorDTO;
import com.livraria.pedido.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class TentativaNaoPermitidaException extends RuntimeException implements IErroPadrao {

    @Override
    public ErrorDTO toErrorDTO() {
        return new ErrorDTO("TENTATIVA_NAO_PERMITIDA",
                "Só é possível tentar de novo um pedido com o pagamento recusado.", HttpStatus.CONFLICT);
    }
}