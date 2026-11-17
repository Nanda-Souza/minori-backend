package com.db.minori_backend.domain.service;

import com.db.minori_backend.domain.dto.CategoriaRequest;
import com.db.minori_backend.domain.dto.CategoriaResponse;
import com.db.minori_backend.domain.entity.Categoria;
import com.db.minori_backend.domain.repository.CategoriaRepository;
import com.db.minori_backend.domain.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.db.minori_backend.domain.exception.RecursoNaoEncontradoException;
import com.db.minori_backend.domain.exception.RegraNegocioException;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;

    public CategoriaService(
            CategoriaRepository categoriaRepository,
            ProdutoRepository produtoRepository
    ) {
        this.categoriaRepository = categoriaRepository;
        this.produtoRepository = produtoRepository;
    }

    private CategoriaResponse toResponse(Categoria categoria) {
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNome()
        );
    }

    @Transactional
    public CategoriaResponse cadastrar(CategoriaRequest request) {
        Categoria categoria = new Categoria();
        categoria.setNome(request.nome());

        Categoria salva = categoriaRepository.save(categoria);

        return toResponse(salva);
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarTodos() {
        return categoriaRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse buscarPorId(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Categoria não encontrada."));

        return toResponse(categoria);
    }

    @Transactional
    public CategoriaResponse atualizar(Long id, CategoriaRequest request) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Categoria não encontrada."));

        categoria.setNome(request.nome());

        Categoria atualizada = categoriaRepository.save(categoria);

        return toResponse(atualizada);
    }

    @Transactional
    public void excluir(Long id) {

        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Categoria não encontrada."));

        if (produtoRepository.existsByCategoriaId(id)) {
            throw new RegraNegocioException(
                    "Não é possível excluir a categoria, pois existem produtos associados!"
            );
        }

        categoriaRepository.delete(categoria);
    }


}