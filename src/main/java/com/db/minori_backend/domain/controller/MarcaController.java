package com.db.minori_backend.domain.controller;

import com.db.minori_backend.domain.dto.MarcaRequest;
import com.db.minori_backend.domain.dto.MarcaResponse;
import com.db.minori_backend.domain.service.MarcaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/marcas")
public class MarcaController {

    private final MarcaService marcaService;

    public MarcaController(MarcaService marcaService) {
        this.marcaService = marcaService;
    }

    @PostMapping
    public ResponseEntity<MarcaResponse> cadastrar(
            @Valid @RequestBody MarcaRequest request) {

        MarcaResponse response = marcaService.cadastrar(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<MarcaResponse>> listarTodos() {

        return ResponseEntity.ok(marcaService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MarcaResponse> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(marcaService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MarcaResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody MarcaRequest request) {

        return ResponseEntity.ok(marcaService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {

        marcaService.excluir(id);

        return ResponseEntity.noContent().build();
    }
}