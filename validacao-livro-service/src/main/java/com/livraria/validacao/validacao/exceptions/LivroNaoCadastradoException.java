package com.livraria.validacao.validacao.exceptions;

import com.livraria.validacao.shared.exception.ErrorDTO;
import com.livraria.validacao.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class LivroNaoCadastradoException extends RuntimeException implements IErroPadrao {
    @Override
    public ErrorDTO toErrorDTO() {
        return new ErrorDTO("LIVRO_NAO_CADASTRADO", "Livro não cadastrado.", HttpStatus.NOT_FOUND);
    }
}
