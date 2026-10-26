package com.db.minori_backend.domain.mapper;

import com.db.minori_backend.domain.dto.ProdutoResponse;
import com.db.minori_backend.domain.entity.Produto;
import org.springframework.stereotype.Component;

@Component
public class ProdutoMapper {

    public ProdutoResponse toResponse(Produto produto) {
        return new ProdutoResponse(
                produto.getId(),
                produto.getNome(),
                produto.getPreco(),
                produto.getQuantidade(),
                produto.getDescricao(),
                produto.getImagemUrl(),
                produto.getCatalogo().getId(),
                produto.getCatalogo().getNome(),
                produto.getMarca().getId(),
                produto.getMarca().getNome(),
                produto.getCategoria().getId(),
                produto.getCategoria().getNome()
        );
    }
}