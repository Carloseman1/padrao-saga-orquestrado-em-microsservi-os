package com.livraria.fiscal.fiscal.exceptions;

import com.livraria.fiscal.shared.exception.ErrorDTO;
import com.livraria.fiscal.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class NotaFiscalDuplicadaException extends RuntimeException implements IErroPadrao {
    @Override
    public ErrorDTO toErrorDTO() {
        return new ErrorDTO("NOTA_FISCAL_DUPLICADA", "Já existe uma NF-e para esta transação.", HttpStatus.CONFLICT);
    }
}
