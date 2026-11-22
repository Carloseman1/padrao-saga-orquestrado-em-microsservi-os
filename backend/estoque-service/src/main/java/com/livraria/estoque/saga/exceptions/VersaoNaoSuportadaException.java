package com.livraria.estoque.saga.exceptions;

import com.livraria.estoque.shared.exception.ErrorDTO;
import com.livraria.estoque.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class VersaoNaoSuportadaException extends RuntimeException implements IErroPadrao {

    public VersaoNaoSuportadaException(Integer versaoRecebida) {
        super("Versão do evento recebida: " + versaoRecebida);
    }

    @Override
    public ErrorDTO toErrorDTO() {
        return new ErrorDTO("VERSAO_NAO_SUPORTADA", "Esta versão do evento não é suportada.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
