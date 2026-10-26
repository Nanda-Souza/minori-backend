package com.db.minori_backend.domain.dto;

import java.math.BigDecimal;

public record ProdutoResponse(
        Long id,
        String nome,
        BigDecimal preco,
        Integer quantidade,
        String descricao,
        String imagemUrl,
        Long catalogoId,
        String catalogoNome,
        Long marcaId,
        String marcaNome,
        Long categoriaId,
        String categoriaNome
) {}