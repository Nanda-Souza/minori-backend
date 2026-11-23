
package com.db.minori_backend.domain.controller;

import com.db.minori_backend.domain.dto.ErroResponse;
import com.db.minori_backend.domain.dto.MarcaRequest;
import com.db.minori_backend.domain.dto.MarcaResponse;
import com.db.minori_backend.domain.service.MarcaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Marcas",
        description = "Operações de gerenciamento de marcas."
)
@RestController
@RequestMapping("/marcas")
public class MarcaController {

    private final MarcaService marcaService;

    public MarcaController(MarcaService marcaService) {
        this.marcaService = marcaService;
    }

    @Operation(summary = "Cadastrar uma nova marca")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Marca cadastrada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos na requisição",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Marca já cadastrada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            )
    })
    @PostMapping
    public ResponseEntity<MarcaResponse> cadastrar(
            @Valid @RequestBody MarcaRequest request) {

        MarcaResponse response = marcaService.cadastrar(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Listar todas as marcas")
    @ApiResponse(
            responseCode = "200",
            description = "Marcas listadas com sucesso"
    )
    @GetMapping
    public ResponseEntity<List<MarcaResponse>> listarTodos() {

        return ResponseEntity.ok(marcaService.listarTodos());
    }

    @Operation(summary = "Buscar marca por ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Marca encontrada"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Marca não encontrada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<MarcaResponse> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(marcaService.buscarPorId(id));
    }

    @Operation(summary = "Atualizar uma marca")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Marca atualizada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos na requisição",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Marca não encontrada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Marca já cadastrada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<MarcaResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody MarcaRequest request) {

        return ResponseEntity.ok(marcaService.atualizar(id, request));
    }

    @Operation(summary = "Excluir uma marca")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Marca excluída com sucesso",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Marca não encontrada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Não é possível excluir uma marca com produtos associados",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {

        marcaService.excluir(id);

        return ResponseEntity.noContent().build();
    }
}
