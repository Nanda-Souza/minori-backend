package com.db.minori_backend.domain.controller;

import com.db.minori_backend.domain.dto.CatalogoRequest;
import com.db.minori_backend.domain.dto.CatalogoResponse;
import com.db.minori_backend.domain.service.CatalogoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/catalogos")
public class CatalogoController {

    private final CatalogoService catalogoService;

    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @PostMapping
    public ResponseEntity<CatalogoResponse> cadastrar(
            @Valid @RequestBody CatalogoRequest request) {

        CatalogoResponse response = catalogoService.cadastrar(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CatalogoResponse>> listarTodos() {

        return ResponseEntity.ok(catalogoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CatalogoResponse> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(catalogoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CatalogoResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody CatalogoRequest request) {

        return ResponseEntity.ok(catalogoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {

        catalogoService.excluir(id);

        return ResponseEntity.noContent().build();
    }
}