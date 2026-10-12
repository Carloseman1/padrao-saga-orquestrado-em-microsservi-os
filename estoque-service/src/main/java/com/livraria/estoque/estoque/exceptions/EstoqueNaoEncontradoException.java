package com.livraria.estoque.estoque.exceptions;

import com.livraria.estoque.shared.exception.ErrorDTO;
import com.livraria.estoque.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class EstoqueNaoEncontradoException extends RuntimeException implements IErroPadrao {
    @Override
    public ErrorDTO toErrorDTO() {
        return new ErrorDTO("ESTOQUE_NAO_ENCONTRADO", "Livro sem estoque cadastrado.", HttpStatus.NOT_FOUND);
    }
}
