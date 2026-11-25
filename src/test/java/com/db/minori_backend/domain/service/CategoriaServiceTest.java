
package com.db.minori_backend.domain.service;

import com.db.minori_backend.domain.dto.CategoriaRequest;
import com.db.minori_backend.domain.dto.CategoriaResponse;
import com.db.minori_backend.domain.entity.Categoria;
import com.db.minori_backend.domain.exception.RecursoNaoEncontradoException;
import com.db.minori_backend.domain.exception.RegraNegocioException;
import com.db.minori_backend.domain.repository.CategoriaRepository;
import com.db.minori_backend.domain.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    private Categoria categoria;
    private CategoriaRequest request;
    private CategoriaResponse response;

    @BeforeEach
    void setUp() {

        categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNome("Macarrão instantâneo");

        request = new CategoriaRequest(
                "Macarrão instantâneo"
        );

        response = new CategoriaResponse(
                1L,
                "Macarrão instantâneo"
        );
    }

    // =========================
    // CADASTRAR
    // =========================

    @Test
    void deveCadastrarCategoriaComSucesso() {

        when(categoriaRepository.existsByNomeIgnoreCase(
                request.nome()
        )).thenReturn(false);

        when(categoriaRepository.save(any(Categoria.class)))
                .thenReturn(categoria);

        CategoriaResponse resultado =
                categoriaService.cadastrar(request);

        assertNotNull(resultado);
        assertEquals(response, resultado);

        verify(categoriaRepository)
                .existsByNomeIgnoreCase(request.nome());

        verify(categoriaRepository).save(argThat(
                categoriaSalva ->
                        categoriaSalva.getNome().equals(request.nome())
        ));
    }

    @Test
    void deveLancarExcecaoAoCadastrarCategoriaDuplicada() {

        when(categoriaRepository.existsByNomeIgnoreCase(
                request.nome()
        )).thenReturn(true);

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> categoriaService.cadastrar(request)
        );

        assertEquals(
                "Já existe uma categoria cadastrada com este nome.",
                exception.getMessage()
        );

        verify(categoriaRepository, never())
                .save(any(Categoria.class));
    }

    @Test
    void deveRemoverEspacosAoCadastrarCategoria() {

        CategoriaRequest requestComEspacos =
                new CategoriaRequest(" Macarrão instantâneo ");

        when(categoriaRepository.existsByNomeIgnoreCase(
                "Macarrão instantâneo"
        )).thenReturn(false);

        when(categoriaRepository.save(any(Categoria.class)))
                .thenAnswer(invocation -> {

                    Categoria categoriaSalva =
                            invocation.getArgument(0);

                    categoriaSalva.setId(1L);

                    return categoriaSalva;
                });

        CategoriaResponse resultado =
                categoriaService.cadastrar(requestComEspacos);

        assertEquals(1L, resultado.id());

        assertEquals(
                "Macarrão instantâneo",
                resultado.nome()
        );

        verify(categoriaRepository)
                .existsByNomeIgnoreCase("Macarrão instantâneo");

        verify(categoriaRepository).save(argThat(
                categoriaSalva -> categoriaSalva.getNome()
                        .equals("Macarrão instantâneo")
        ));
    }

    // =========================
    // LISTAR TODOS
    // =========================

    @Test
    void deveListarTodasAsCategorias() {

        Categoria segundaCategoria = new Categoria();
        segundaCategoria.setId(2L);
        segundaCategoria.setNome("Biscoitos");

        when(categoriaRepository.findAll())
                .thenReturn(List.of(
                        categoria,
                        segundaCategoria
                ));

        List<CategoriaResponse> resultado =
                categoriaService.listarTodos();

        assertEquals(2, resultado.size());

        assertEquals(
                List.of(
                        response,
                        new CategoriaResponse(2L, "Biscoitos")
                ),
                resultado
        );

        verify(categoriaRepository).findAll();
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremCategorias() {

        when(categoriaRepository.findAll())
                .thenReturn(List.of());

        List<CategoriaResponse> resultado =
                categoriaService.listarTodos();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verify(categoriaRepository).findAll();
    }

    // =========================
    // BUSCAR POR ID
    // =========================

    @Test
    void deveBuscarCategoriaPorId() {

        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));

        CategoriaResponse resultado =
                categoriaService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals(response, resultado);

        verify(categoriaRepository).findById(1L);
    }

    @Test
    void deveLancarExcecaoAoBuscarCategoriaInexistente() {

        when(categoriaRepository.findById(99L))
                .thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> categoriaService.buscarPorId(99L)
        );

        assertEquals(
                "Categoria não encontrada.",
                exception.getMessage()
        );

        verify(categoriaRepository).findById(99L);
    }

    // =========================
    // ATUALIZAR
    // =========================

    @Test
    void deveAtualizarCategoriaComSucesso() {

        CategoriaRequest requestAtualizacao =
                new CategoriaRequest(" Macarrões asiáticos ");

        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));

        when(categoriaRepository.existsByNomeIgnoreCaseAndIdNot(
                "Macarrões asiáticos",
                1L
        )).thenReturn(false);

        when(categoriaRepository.save(categoria))
                .thenReturn(categoria);

        CategoriaResponse resultado =
                categoriaService.atualizar(1L, requestAtualizacao);

        assertNotNull(resultado);
        assertEquals(1L, resultado.id());

        assertEquals(
                "Macarrões asiáticos",
                resultado.nome()
        );

        assertEquals(
                "Macarrões asiáticos",
                categoria.getNome()
        );

        verify(categoriaRepository)
                .existsByNomeIgnoreCaseAndIdNot(
                        "Macarrões asiáticos",
                        1L
                );

        verify(categoriaRepository).save(categoria);
    }

    @Test
    void deveAtualizarCategoriaMantendoProprioNome() {

        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));

        when(categoriaRepository.existsByNomeIgnoreCaseAndIdNot(
                request.nome(),
                1L
        )).thenReturn(false);

        when(categoriaRepository.save(categoria))
                .thenReturn(categoria);

        CategoriaResponse resultado =
                categoriaService.atualizar(1L, request);

        assertEquals(response, resultado);

        verify(categoriaRepository)
                .existsByNomeIgnoreCaseAndIdNot(
                        request.nome(),
                        1L
                );

        verify(categoriaRepository).save(categoria);
    }

    @Test
    void deveLancarExcecaoAoAtualizarComNomeDuplicado() {

        CategoriaRequest requestDuplicado =
                new CategoriaRequest("Biscoitos");

        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));

        when(categoriaRepository.existsByNomeIgnoreCaseAndIdNot(
                "Biscoitos",
                1L
        )).thenReturn(true);

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> categoriaService.atualizar(
                        1L,
                        requestDuplicado
                )
        );

        assertEquals(
                "Já existe uma categoria cadastrada com este nome.",
                exception.getMessage()
        );

        assertEquals(
                "Macarrão instantâneo",
                categoria.getNome()
        );

        verify(categoriaRepository, never())
                .save(any(Categoria.class));
    }

    @Test
    void deveLancarExcecaoAoAtualizarCategoriaInexistente() {

        when(categoriaRepository.findById(99L))
                .thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> categoriaService.atualizar(99L, request)
        );

        assertEquals(
                "Categoria não encontrada.",
                exception.getMessage()
        );

        verify(categoriaRepository, never())
                .existsByNomeIgnoreCaseAndIdNot(
                        anyString(),
                        anyLong()
                );

        verify(categoriaRepository, never())
                .save(any(Categoria.class));
    }

    // =========================
    // EXCLUIR
    // =========================

    @Test
    void deveExcluirCategoriaComSucesso() {

        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));

        when(produtoRepository.existsByCategoriaId(1L))
                .thenReturn(false);

        categoriaService.excluir(1L);

        verify(categoriaRepository).findById(1L);

        verify(produtoRepository).existsByCategoriaId(1L);

        verify(categoriaRepository).delete(categoria);
    }

    @Test
    void deveLancarExcecaoAoExcluirCategoriaInexistente() {

        when(categoriaRepository.findById(99L))
                .thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> categoriaService.excluir(99L)
        );

        assertEquals(
                "Categoria não encontrada.",
                exception.getMessage()
        );

        verify(categoriaRepository, never())
                .delete(any(Categoria.class));

        verifyNoInteractions(produtoRepository);
    }

    @Test
    void deveLancarExcecaoAoExcluirCategoriaComProdutosAssociados() {

        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));

        when(produtoRepository.existsByCategoriaId(1L))
                .thenReturn(true);

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> categoriaService.excluir(1L)
        );

        assertEquals(
                "Não é possível excluir a categoria, pois existem produtos associados!",
                exception.getMessage()
        );

        verify(produtoRepository).existsByCategoriaId(1L);

        verify(categoriaRepository, never())
                .delete(any(Categoria.class));
    }
}
