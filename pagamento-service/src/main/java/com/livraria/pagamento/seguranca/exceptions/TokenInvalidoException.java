package com.livraria.pagamento.seguranca.exceptions;

import com.livraria.pagamento.shared.exception.ErrorDTO;
import com.livraria.pagamento.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class TokenInvalidoException  extends RuntimeException implements IErroPadrao {

    @Override
    public ErrorDTO toErrorDTO(){
        return new ErrorDTO("TOKEN_INVALIDO", "Token ausente ou inválido. Faça login novamente",
                HttpStatus.UNAUTHORIZED);
    }
}
