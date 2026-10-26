package com.livraria.pagamento.cartao.exceptions;

import com.livraria.pagamento.shared.exception.ErrorDTO;
import com.livraria.pagamento.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class CartaoNaoEncontradoException extends RuntimeException implements IErroPadrao {
    @Override
    public ErrorDTO toErrorDTO() {
        return new ErrorDTO("CARTAO_NAO_ENCONTRADO", "Cartão não encontrado.", HttpStatus.NOT_FOUND);
    }
}
