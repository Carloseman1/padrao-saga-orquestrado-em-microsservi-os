package com.livraria.pagamento.pagamento.exceptions;

import com.livraria.pagamento.shared.exception.ErrorDTO;
import com.livraria.pagamento.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class ValorMinimoNaoAtingidoException extends RuntimeException implements IErroPadrao {
    @Override
    public ErrorDTO toErrorDTO() {
        return new ErrorDTO("VALOR_MINIMO_NAO_ATINGIDO", "O valor mínimo para pagamento é R$ 0,10.", HttpStatus.BAD_REQUEST);
    }
}
