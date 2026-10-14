package com.db.minori_backend.domain.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProdutoRequest(

        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
        String nome,

        @NotNull(message = "O preço é obrigatório.")
        @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero.")
        BigDecimal preco,

        @NotNull(message = "A quantidade é obrigatória.")
        @PositiveOrZero(message = "A quantidade não pode ser negativa.")
        Integer quantidade,

        @NotNull(message = "A descrição é obrigatória.")
        @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres.")
        String descricao,

        @Size(max = 500, message = "A URL da imagem deve ter no máximo 500 caracteres.")
        String imagemUrl,

        @NotNull(message = "O catálogo é obrigatório.")
        @Positive(message = "O ID do catálogo deve ser positivo.")
        Long catalogoId,

        @NotNull(message = "A marca é obrigatória.")
        @Positive(message = "O ID da marca deve ser positivo.")
        Long marcaId,

        @NotNull(message = "A categoria é obrigatória.")
        @Positive(message = "O ID da categoria deve ser positivo.")
        Long categoriaId

) {}