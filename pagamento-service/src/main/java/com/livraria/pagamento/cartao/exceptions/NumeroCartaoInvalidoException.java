package com.livraria.pagamento.cartao.exceptions;

import com.livraria.pagamento.shared.exception.ErrorDTO;
import com.livraria.pagamento.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class NumeroCartaoInvalidoException extends RuntimeException implements IErroPadrao {

    @Override
    public ErrorDTO toErrorDTO() {
        return new ErrorDTO(
                "NUMERO_CARTAO_INVALIDO",
                "O número do cartão deve ter entre 13 e 19 dígitos e passar na validação de Luhn.", HttpStatus.BAD_REQUEST
        );
    }
}
