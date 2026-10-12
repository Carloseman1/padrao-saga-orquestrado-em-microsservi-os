package com.livraria.validacao.validacao.exceptions;

import com.livraria.validacao.shared.exception.ErrorDTO;
import com.livraria.validacao.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class LivroNaoInformadoException extends RuntimeException implements IErroPadrao {
    @Override
    public ErrorDTO toErrorDTO() {
        return new ErrorDTO("LIVRO_NAO_INFORMADO", "Informe o livro de cada item do pedido.", HttpStatus.BAD_REQUEST);
    }
}
