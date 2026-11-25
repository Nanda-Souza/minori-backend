package com.db.minori_backend.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MarcaRequest(

        @NotBlank(message = "O nome da marca é obrigatório.")
        @Size(max = 200, message = "O nome deve ter no máximo 200 caracteres.")
        String nome

) {}