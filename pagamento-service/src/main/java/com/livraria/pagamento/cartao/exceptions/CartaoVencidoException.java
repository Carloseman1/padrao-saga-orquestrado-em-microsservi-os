package com.livraria.pagamento.cartao.exceptions;

import com.livraria.pagamento.shared.exception.ErrorDTO;
import com.livraria.pagamento.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class CartaoVencidoException extends RuntimeException implements IErroPadrao {
    @Override
    public ErrorDTO toErrorDTO() {
        return new ErrorDTO("CARTAO_VENCIDO", "O cartão já está vencido.", HttpStatus.BAD_REQUEST);
    }
}
