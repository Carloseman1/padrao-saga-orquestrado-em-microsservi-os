package com.livraria.validacao.catalogo.exceptions;

import com.livraria.validacao.shared.exception.ErrorDTO;
import com.livraria.validacao.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class LivroJaCadastradoException extends RuntimeException implements IErroPadrao {
    @Override
    public ErrorDTO toErrorDTO() {
        return new ErrorDTO("LIVRO_JA_CADASTRADO", "Já existe um livro com este código.", HttpStatus.CONFLICT);
    }
}
