package com.livraria.orquestrador.orquestracao.exceptions;

import com.livraria.orquestrador.shared.exception.ErrorDTO;
import com.livraria.orquestrador.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class TransicaoNaoEncontradaException extends RuntimeException implements IErroPadrao {
    @Override
    public ErrorDTO toErrorDTO() {
        return new ErrorDTO("TRANSICAO_NAO_ENCONTRADA", "Nenhuma transição da saga para a origem e o status informados.", HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
