
package com.db.minori_backend.domain.controller;

import com.db.minori_backend.domain.dto.CatalogoRequest;
import com.db.minori_backend.domain.dto.CatalogoResponse;
import com.db.minori_backend.domain.dto.ErroResponse;
import com.db.minori_backend.domain.service.CatalogoService;
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
        name = "Catálogos",
        description = "Operações de gerenciamento de catálogos."
)
@RestController
@RequestMapping("/catalogos")
public class CatalogoController {

    private final CatalogoService catalogoService;

    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @Operation(summary = "Cadastrar um novo catálogo")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Catálogo cadastrado com sucesso"
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
                    description = "Catálogo já cadastrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            )
    })
    @PostMapping
    public ResponseEntity<CatalogoResponse> cadastrar(
            @Valid @RequestBody CatalogoRequest request) {

        CatalogoResponse response = catalogoService.cadastrar(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Listar todos os catálogos")
    @ApiResponse(
            responseCode = "200",
            description = "Catálogos listados com sucesso"
    )
    @GetMapping
    public ResponseEntity<List<CatalogoResponse>> listarTodos() {

        return ResponseEntity.ok(catalogoService.listarTodos());
    }

    @Operation(summary = "Buscar catálogo por ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Catálogo encontrado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Catálogo não encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<CatalogoResponse> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(catalogoService.buscarPorId(id));
    }

    @Operation(summary = "Atualizar um catálogo")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Catálogo atualizado com sucesso"
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
                    description = "Catálogo não encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Catálogo já cadastrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<CatalogoResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody CatalogoRequest request) {

        return ResponseEntity.ok(catalogoService.atualizar(id, request));
    }

    @Operation(summary = "Excluir um catálogo")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Catálogo excluído com sucesso",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Catálogo não encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Não é possível excluir um catálogo com produtos associados",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {

        catalogoService.excluir(id);

        return ResponseEntity.noContent().build();
    }
}
