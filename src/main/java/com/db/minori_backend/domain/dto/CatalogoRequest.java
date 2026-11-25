package com.db.minori_backend.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CatalogoRequest(

        @NotBlank(message = "O nome do catálogo é obrigatório.")
        @Size(max = 200, message = "O nome deve ter no máximo 200 caracteres.")
        String nome

) {}