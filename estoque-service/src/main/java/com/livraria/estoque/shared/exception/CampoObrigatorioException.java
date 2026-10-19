package com.livraria.estoque.shared.exception;

import org.springframework.http.HttpStatus;

public class CampoObrigatorioException extends RuntimeException implements IErroPadrao {

    private final String campo;

    public CampoObrigatorioException(String campo) {
        this.campo = campo;
    }

    @Override
    public ErrorDTO toErrorDTO() {
        return new ErrorDTO("CAMPO_OBRIGATORIO", "Informe o " + this.campo + ".", HttpStatus.BAD_REQUEST);
    }
}
