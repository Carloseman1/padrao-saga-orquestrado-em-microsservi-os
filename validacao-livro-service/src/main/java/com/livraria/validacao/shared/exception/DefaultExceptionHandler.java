package com.livraria.validacao.shared.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class DefaultExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(DefaultExceptionHandler.class);

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorDTO> handleRotaNaoEncontrada(NoResourceFoundException ex) {
        return resposta(new ErrorDTO(
                "ROTA_NAO_ENCONTRADA", "Esta rota não existe.", HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorDTO> handleMetodoNaoPermitido(HttpRequestMethodNotSupportedException ex) {
        return resposta(new ErrorDTO(
                "METODO_NAO_PERMITIDO", "Esta rota não aceita este método HTTP.",
                HttpStatus.METHOD_NOT_ALLOWED));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDTO> handleCorpoInvalido(HttpMessageNotReadableException ex) {
        return resposta(new ErrorDTO(
                "CORPO_INVALIDO", "O corpo da requisição não é um JSON válido.",
                HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDTO> handleParametroInvalido(MethodArgumentTypeMismatchException ex) {
        return resposta(new ErrorDTO(
                "PARAMETRO_INVALIDO", "Identificador inválido.", HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> handleDefault(Exception ex) {
        if (ex instanceof IErroPadrao erro) {
            return resposta(erro.toErrorDTO());
        }

        log.error("Erro nao mapeado", ex);

        return resposta(new ErrorDTO(
                "ERRO_NAO_MAPEADO", "Ocorreu um erro inesperado.",
                HttpStatus.INTERNAL_SERVER_ERROR));
    }

    private ResponseEntity<ErrorDTO> resposta(ErrorDTO error) {
        return ResponseEntity.status(error.status()).body(error);
    }
}
