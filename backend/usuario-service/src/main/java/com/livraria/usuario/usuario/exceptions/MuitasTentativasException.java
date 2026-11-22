package com.livraria.usuario.usuario.exceptions;

import com.livraria.usuario.shared.exception.ErrorDTO;
import com.livraria.usuario.shared.exception.IErroPadrao;
import org.springframework.http.HttpStatus;

public class MuitasTentativasException extends RuntimeException implements IErroPadrao {
    @Override
    public ErrorDTO toErrorDTO(){
        return new ErrorDTO("MUITOS_TENTATIVAS", "Muitas tentativas de login. Aguarde um minuto e tente de novo.",
                HttpStatus.TOO_MANY_REQUESTS);
    }
}
