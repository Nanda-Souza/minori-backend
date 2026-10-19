package com.db.minori_backend.domain.service;

import com.db.minori_backend.domain.dto.MarcaRequest;
import com.db.minori_backend.domain.dto.MarcaResponse;
import com.db.minori_backend.domain.entity.Marca;
import com.db.minori_backend.domain.repository.MarcaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MarcaService {

    private final MarcaRepository marcaRepository;

    public MarcaService(MarcaRepository marcaRepository) {
        this.marcaRepository = marcaRepository;
    }

    @Transactional
    public MarcaResponse cadastrar(MarcaRequest request) {
        Marca marca = new Marca();
        marca.setNome(request.nome());

        Marca salva = marcaRepository.save(marca);

        return toResponse(salva);
    }

    @Transactional(readOnly = true)
    public List<MarcaResponse> listarTodos() {
        return marcaRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MarcaResponse buscarPorId(Long id) {
        Marca marca = marcaRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Marca não encontrada."));

        return toResponse(marca);
    }

    @Transactional
    public MarcaResponse atualizar(Long id, MarcaRequest request) {
        Marca marca = marcaRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Marca não encontrada."));

        marca.setNome(request.nome());

        Marca atualizada = marcaRepository.save(marca);

        return toResponse(atualizada);
    }

    @Transactional
    public void excluir(Long id) {
        Marca marca = marcaRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Marca não encontrada."));

        marcaRepository.delete(marca);
    }

    private MarcaResponse toResponse(Marca marca) {
        return new MarcaResponse(
                marca.getId(),
                marca.getNome()
        );
    }
}