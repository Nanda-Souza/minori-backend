package com.db.minori_backend.domain.controller;

import com.db.minori_backend.domain.dto.ErroResponse;
import com.db.minori_backend.domain.dto.ProdutoRequest;
import com.db.minori_backend.domain.dto.ProdutoResponse;
import com.db.minori_backend.domain.service.ProdutoService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Tag(
        name = "Produtos",
        description = "Operações de gerenciamento e consulta de produtos."
)
@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @Operation(summary = "Cadastrar um novo produto")
    @ApiResponse(responseCode = "201", description = "Produto cadastrado com sucesso")
    @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos na requisição",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErroResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Catálogo, marca ou categoria não encontrados",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErroResponse.class)
            )
    )
    @PostMapping
    public ResponseEntity<ProdutoResponse> cadastrar(
            @Valid @RequestBody ProdutoRequest request) {

        ProdutoResponse response = produtoService.cadastrar(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Listar todos os produtos")
    @ApiResponse(responseCode = "200", description = "Produtos listados com sucesso")
    @GetMapping
    public ResponseEntity<List<ProdutoResponse>> listarTodos() {

        return ResponseEntity.ok(produtoService.listarTodos());
    }

    @Operation(summary = "Buscar produto por ID")
    @ApiResponse(responseCode = "200", description = "Produto encontrado")
    @ApiResponse(
            responseCode = "404",
            description = "Produto não encontrado",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErroResponse.class)
            )
    )
    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponse> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(produtoService.buscarPorId(id));
    }

    @Operation(summary = "Atualizar um produto")
    @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso")
    @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos na requisição",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErroResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Produto ou relacionamento não encontrado",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErroResponse.class)
            )
    )
    @PutMapping("/{id}")
    public ResponseEntity<ProdutoResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProdutoRequest request) {

        return ResponseEntity.ok(produtoService.atualizar(id, request));
    }

    @Operation(summary = "Excluir um produto")
    @ApiResponse(responseCode = "204", description = "Produto excluído com sucesso")
    @ApiResponse(
            responseCode = "404",
            description = "Produto não encontrado",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErroResponse.class)
            )
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {

        produtoService.excluir(id);

        return ResponseEntity.noContent().build();
    }

    @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso")
    @ApiResponse(
            responseCode = "400",
            description = "Parâmetros de filtro inválidos",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErroResponse.class)
            )
    )
    @GetMapping("/filtrar")
    public ResponseEntity<List<ProdutoResponse>> filtrar(
            @RequestParam(required = false) Long catalogoId,
            @RequestParam(required = false) Long marcaId,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) BigDecimal precoMin,
            @RequestParam(required = false) BigDecimal precoMax
    ) {

        return ResponseEntity.ok(
                produtoService.filtrar(
                        catalogoId,
                        marcaId,
                        categoriaId,
                        precoMin,
                        precoMax
                )
        );
    }
}