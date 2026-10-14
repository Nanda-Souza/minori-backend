package com.db.minori_backend.domain.service;

import com.db.minori_backend.domain.dto.CatalogoRequest;
import com.db.minori_backend.domain.dto.CatalogoResponse;
import com.db.minori_backend.domain.entity.Catalogo;
import com.db.minori_backend.domain.repository.CatalogoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CatalogoService {

    private final CatalogoRepository catalogoRepository;

    public CatalogoService(CatalogoRepository catalogoRepository) {
        this.catalogoRepository = catalogoRepository;
    }

    private CatalogoResponse toResponse(Catalogo catalogo) {
        return new CatalogoResponse(
                catalogo.getId(),
                catalogo.getNome()
        );
    }

    @Transactional
    public CatalogoResponse cadastrar(CatalogoRequest request) {
        Catalogo catalogo = new Catalogo();
        catalogo.setNome(request.nome());

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
                        new RuntimeException("Catálogo não encontrado."));

        return toResponse(catalogo);
    }

    @Transactional
    public CatalogoResponse atualizar(Long id, CatalogoRequest request) {
        Catalogo catalogo = catalogoRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Catálogo não encontrado."));

        catalogo.setNome(request.nome());

        Catalogo atualizado = catalogoRepository.save(catalogo);

        return toResponse(atualizado);
    }

    @Transactional
    public void excluir(Long id) {
        Catalogo catalogo = catalogoRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Catálogo não encontrado."));

        catalogoRepository.delete(catalogo);
    }


}