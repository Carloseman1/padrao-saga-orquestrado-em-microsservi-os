package com.livraria.pedido.shared.exception;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record ErrorDTO(String code, String message, @JsonIgnore HttpStatus status) {
}
