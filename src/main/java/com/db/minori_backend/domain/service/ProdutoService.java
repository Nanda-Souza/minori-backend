package com.db.minori_backend.domain.service;

import com.db.minori_backend.domain.dto.ProdutoRequest;
import com.db.minori_backend.domain.dto.ProdutoResponse;
import com.db.minori_backend.domain.entity.*;
import com.db.minori_backend.domain.mapper.ProdutoMapper;
import com.db.minori_backend.domain.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.db.minori_backend.domain.exception.RecursoNaoEncontradoException;
import com.db.minori_backend.domain.exception.RegraNegocioException;

import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final CatalogoRepository catalogoRepository;
    private final MarcaRepository marcaRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProdutoMapper produtoMapper;

    public ProdutoService(
            ProdutoRepository produtoRepository,
            CatalogoRepository catalogoRepository,
            MarcaRepository marcaRepository,
            CategoriaRepository categoriaRepository,
            ProdutoMapper produtoMapper
    ) {
        this.produtoRepository = produtoRepository;
        this.catalogoRepository = catalogoRepository;
        this.marcaRepository = marcaRepository;
        this.categoriaRepository = categoriaRepository;
        this.produtoMapper = produtoMapper;
    }

    @Transactional
    public ProdutoResponse cadastrar(ProdutoRequest request) {

        Catalogo catalogo = buscarCatalogo(request.catalogoId());
        Marca marca = buscarMarca(request.marcaId());
        Categoria categoria = buscarCategoria(request.categoriaId());

        Produto produto = new Produto();

        preencherProduto(produto, request, catalogo, marca, categoria);

        Produto salvo = produtoRepository.save(produto);

        return produtoMapper.toResponse(salvo);
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listarTodos() {

        return produtoRepository.findAll()
                .stream()
                .map(produtoMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProdutoResponse buscarPorId(Long id) {

        Produto produto = buscarProduto(id);

        return produtoMapper.toResponse(produto);
    }

    @Transactional
    public ProdutoResponse atualizar(Long id, ProdutoRequest request) {

        Produto produto = buscarProduto(id);

        Catalogo catalogo = buscarCatalogo(request.catalogoId());
        Marca marca = buscarMarca(request.marcaId());
        Categoria categoria = buscarCategoria(request.categoriaId());

        preencherProduto(produto, request, catalogo, marca, categoria);

        Produto atualizado = produtoRepository.save(produto);

        return produtoMapper.toResponse(atualizado);
    }

    @Transactional
    public void excluir(Long id) {

        Produto produto = buscarProduto(id);

        produtoRepository.delete(produto);
    }

    private Produto buscarProduto(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Produto não encontrado."));
    }

    private Catalogo buscarCatalogo(Long id) {
        return catalogoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Catálogo não encontrado."));
    }

    private Marca buscarMarca(Long id) {
        return marcaRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Marca não encontrada."));
    }

    private Categoria buscarCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Categoria não encontrada."));
    }

    private void preencherProduto(
            Produto produto,
            ProdutoRequest request,
            Catalogo catalogo,
            Marca marca,
            Categoria categoria
    ) {
        produto.setNome(request.nome());
        produto.setPreco(request.preco());
        produto.setQuantidade(request.quantidade());
        produto.setDescricao(request.descricao());
        produto.setImagemUrl(request.imagemUrl());

        produto.setCatalogo(catalogo);
        produto.setMarca(marca);
        produto.setCategoria(categoria);
    }
}