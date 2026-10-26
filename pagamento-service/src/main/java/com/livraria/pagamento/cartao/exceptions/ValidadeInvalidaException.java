package com.livraria.pagamento.cartao.exceptions;

import com.livraria.pagamento.shared.exception.ErrorDTO;
import com.livraria.pagamento.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class ValidadeInvalidaException extends RuntimeException implements IErroPadrao {
    @Override
    public ErrorDTO toErrorDTO() {
        return new ErrorDTO(
                "VALIDADE_INVALIDA",
                "Informe o mês entre 1 e 12 e o ano com quatro dígitos.", HttpStatus.BAD_REQUEST
        );
    }
}
