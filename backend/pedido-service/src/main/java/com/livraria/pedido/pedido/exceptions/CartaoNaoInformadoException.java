package com.livraria.pedido.pedido.exceptions;

import com.livraria.pedido.shared.exception.ErrorDTO;
import com.livraria.pedido.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class CartaoNaoInformadoException extends RuntimeException implements IErroPadrao {
    @Override
    public ErrorDTO toErrorDTO() {
        return new ErrorDTO("CARTAO_NAO_INFORMADO", "Informe o cartão para o pagamento.", HttpStatus.BAD_REQUEST);
    }
}
