
package com.db.minori_backend.domain.service;

import com.db.minori_backend.domain.dto.CatalogoRequest;
import com.db.minori_backend.domain.dto.CatalogoResponse;
import com.db.minori_backend.domain.entity.Catalogo;
import com.db.minori_backend.domain.exception.RecursoNaoEncontradoException;
import com.db.minori_backend.domain.exception.RegraNegocioException;
import com.db.minori_backend.domain.repository.CatalogoRepository;
import com.db.minori_backend.domain.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CatalogoService {

    private final CatalogoRepository catalogoRepository;
    private final ProdutoRepository produtoRepository;

    public CatalogoService(
            CatalogoRepository catalogoRepository,
            ProdutoRepository produtoRepository
    ) {
        this.catalogoRepository = catalogoRepository;
        this.produtoRepository = produtoRepository;
    }

    private CatalogoResponse toResponse(Catalogo catalogo) {
        return new CatalogoResponse(
                catalogo.getId(),
                catalogo.getNome()
        );
    }

    private void validarNomeDuplicado(String nome, Long id) {

        boolean existe = id == null
                ? catalogoRepository.existsByNomeIgnoreCase(nome)
                : catalogoRepository.existsByNomeIgnoreCaseAndIdNot(nome, id);

        if (existe) {
            throw new RegraNegocioException(
                    "Já existe um catálogo cadastrado com este nome."
            );
        }
    }

    @Transactional
    public CatalogoResponse cadastrar(CatalogoRequest request) {

        String nome = request.nome().trim();

        validarNomeDuplicado(nome, null);

        Catalogo catalogo = new Catalogo();
        catalogo.setNome(nome);

        Catalogo salvo = catalogoRepository.save(catalogo);

        return toResponse(salvo);
    }

    @Transactional(readOnly = true)
    public List<CatalogoResponse> listarTodos() {
        return catalogoRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CatalogoResponse buscarPorId(Long id) {
        Catalogo catalogo = catalogoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Catálogo não encontrado."));

        return toResponse(catalogo);
    }

    @Transactional
    public CatalogoResponse atualizar(Long id, CatalogoRequest request) {

        Catalogo catalogo = catalogoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Catálogo não encontrado."));

        String nome = request.nome().trim();

        validarNomeDuplicado(nome, id);

        catalogo.setNome(nome);

        Catalogo atualizado = catalogoRepository.save(catalogo);

        return toResponse(atualizado);
    }

    @Transactional
    public void excluir(Long id) {

        Catalogo catalogo = catalogoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Catálogo não encontrado."));

        if (produtoRepository.existsByCatalogoId(id)) {
            throw new RegraNegocioException(
                    "Não é possível excluir o catálogo, pois existem produtos associados!"
            );
        }

        catalogoRepository.delete(catalogo);
    }
}
