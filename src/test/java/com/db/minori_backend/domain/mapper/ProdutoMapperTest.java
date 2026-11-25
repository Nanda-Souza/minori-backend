
package com.db.minori_backend.domain.mapper;

import com.db.minori_backend.domain.dto.ProdutoResponse;
import com.db.minori_backend.domain.entity.Catalogo;
import com.db.minori_backend.domain.entity.Categoria;
import com.db.minori_backend.domain.entity.Marca;
import com.db.minori_backend.domain.entity.Produto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProdutoMapperTest {

    private final ProdutoMapper produtoMapper = new ProdutoMapper();

    @Test
    void deveConverterProdutoParaProdutoResponse() {

        Catalogo catalogo = new Catalogo();
        catalogo.setId(1L);
        catalogo.setNome("Lamen e Macarrões");

        Marca marca = new Marca();
        marca.setId(2L);
        marca.setNome("Nissin");

        Categoria categoria = new Categoria();
        categoria.setId(3L);
        categoria.setNome("Macarrão instantâneo");

        Produto produto = new Produto();
        produto.setId(10L);
        produto.setNome("Lamen Shoyu");
        produto.setPreco(new BigDecimal("15.90"));
        produto.setQuantidade(10);
        produto.setDescricao("Macarrão instantâneo sabor shoyu.");
        produto.setImagemUrl("https://minori.com/lamen.jpg");

        produto.setCatalogo(catalogo);
        produto.setMarca(marca);
        produto.setCategoria(categoria);

        ProdutoResponse response = produtoMapper.toResponse(produto);

        assertNotNull(response);

        assertAll(
                () -> assertEquals(produto.getId(), response.id()),
                () -> assertEquals(produto.getNome(), response.nome()),
                () -> assertEquals(produto.getPreco(), response.preco()),
                () -> assertEquals(produto.getQuantidade(), response.quantidade()),
                () -> assertEquals(produto.getDescricao(), response.descricao()),
                () -> assertEquals(produto.getImagemUrl(), response.imagemUrl()),

                () -> assertEquals(catalogo.getId(), response.catalogoId()),
                () -> assertEquals(catalogo.getNome(), response.catalogoNome()),

                () -> assertEquals(marca.getId(), response.marcaId()),
                () -> assertEquals(marca.getNome(), response.marcaNome()),

                () -> assertEquals(categoria.getId(), response.categoriaId()),
                () -> assertEquals(categoria.getNome(), response.categoriaNome())
        );
    }
}
