package com.livraria.estoque.estoque.exceptions;

import com.livraria.estoque.shared.exception.ErrorDTO;
import com.livraria.estoque.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class EstoqueInsuficienteException extends RuntimeException implements IErroPadrao {
    @Override
    public ErrorDTO toErrorDTO() {
        return new ErrorDTO("ESTOQUE_INSUFICIENTE", "Estoque insuficiente para o livro.", HttpStatus.BAD_REQUEST);
    }
}
