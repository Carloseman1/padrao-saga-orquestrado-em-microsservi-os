package com.livraria.usuario.usuario.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record LoginResponseDTO(Long id, String nome, String email, @JsonIgnore String token) {}
