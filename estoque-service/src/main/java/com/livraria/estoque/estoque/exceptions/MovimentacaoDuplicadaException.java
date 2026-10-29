package com.livraria.estoque.estoque.exceptions;

import com.livraria.estoque.shared.exception.ErrorDTO;
import com.livraria.estoque.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class MovimentacaoDuplicadaException extends RuntimeException implements IErroPadrao {
    @Override
    public ErrorDTO toErrorDTO() {
        return new ErrorDTO("MOVIMENTACAO_DUPLICADA", "Já existe uma baixa de estoque para esta transação.", HttpStatus.CONFLICT);
    }
}
