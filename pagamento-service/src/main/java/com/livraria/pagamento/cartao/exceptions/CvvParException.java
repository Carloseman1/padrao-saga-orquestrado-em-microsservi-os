package com.livraria.pagamento.cartao.exceptions;

import com.livraria.pagamento.shared.exception.ErrorDTO;
import com.livraria.pagamento.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class CvvParException extends RuntimeException implements IErroPadrao {
    @Override
    public ErrorDTO toErrorDTO() {
        return new ErrorDTO("CVV_PAR", "O CVV não pode ser um número par.", HttpStatus.BAD_REQUEST);
    }
}
