package com.livraria.pagamento.pagamento.exceptions;

import com.livraria.pagamento.shared.exception.ErrorDTO;
import com.livraria.pagamento.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class PagamentoNaoEncontradoException extends RuntimeException implements IErroPadrao {
    @Override
    public ErrorDTO toErrorDTO() {
        return new ErrorDTO("PAGAMENTO_NAO_ENCONTRADO", "Pagamento não encontrado para o pedido e a transação.", HttpStatus.NOT_FOUND);
    }
}
