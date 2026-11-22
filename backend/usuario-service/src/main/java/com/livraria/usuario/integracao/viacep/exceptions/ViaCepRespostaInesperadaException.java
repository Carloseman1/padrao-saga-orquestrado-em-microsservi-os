package com.livraria.usuario.integracao.viacep.exceptions;

import com.livraria.usuario.shared.exception.ErrorDTO;
import com.livraria.usuario.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class ViaCepRespostaInesperadaException extends RuntimeException implements IErroPadrao {

    public ViaCepRespostaInesperadaException(Throwable causa) {
        super(causa);
    }

    public ViaCepRespostaInesperadaException() {
        super();
    }

    @Override
    public ErrorDTO toErrorDTO() {
        return new ErrorDTO(
                "CEP_RESPOSTA_INESPERADA",
                "A consulta de CEP respondeu em um formato que o sistema não reconhece.", HttpStatus.BAD_GATEWAY
        );
    }
}