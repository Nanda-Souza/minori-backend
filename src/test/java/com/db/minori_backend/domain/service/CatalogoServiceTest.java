
package com.db.minori_backend.domain.service;

import com.db.minori_backend.domain.dto.CatalogoRequest;
import com.db.minori_backend.domain.dto.CatalogoResponse;
import com.db.minori_backend.domain.entity.Catalogo;
import com.db.minori_backend.domain.exception.RecursoNaoEncontradoException;
import com.db.minori_backend.domain.exception.RegraNegocioException;
import com.db.minori_backend.domain.repository.CatalogoRepository;
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
class CatalogoServiceTest {

    @Mock
    private CatalogoRepository catalogoRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private CatalogoService catalogoService;

    private Catalogo catalogo;
    private CatalogoRequest request;
    private CatalogoResponse response;

    @BeforeEach
    void setUp() {

        catalogo = new Catalogo();
        catalogo.setId(1L);
        catalogo.setNome("Lamen e Macarrões");

        request = new CatalogoRequest(
                "Lamen e Macarrões"
        );

        response = new CatalogoResponse(
                1L,
                "Lamen e Macarrões"
        );
    }

    // =========================
    // CADASTRAR
    // =========================

    @Test
    void deveCadastrarCatalogoComSucesso() {

        when(catalogoRepository.existsByNomeIgnoreCase(
                request.nome()
        )).thenReturn(false);

        when(catalogoRepository.save(any(Catalogo.class)))
                .thenReturn(catalogo);

        CatalogoResponse resultado =
                catalogoService.cadastrar(request);

        assertNotNull(resultado);
        assertEquals(response, resultado);

        verify(catalogoRepository)
                .existsByNomeIgnoreCase(request.nome());

        verify(catalogoRepository).save(argThat(
                catalogoSalvo ->
                        catalogoSalvo.getNome().equals(request.nome())
        ));
    }

    @Test
    void deveLancarExcecaoAoCadastrarCatalogoDuplicado() {

        when(catalogoRepository.existsByNomeIgnoreCase(
                request.nome()
        )).thenReturn(true);

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> catalogoService.cadastrar(request)
        );

        assertEquals(
                "Já existe um catálogo cadastrado com este nome.",
                exception.getMessage()
        );

        verify(catalogoRepository, never())
                .save(any(Catalogo.class));
    }

    @Test
    void deveRemoverEspacosAoCadastrarCatalogo() {

        CatalogoRequest requestComEspacos =
                new CatalogoRequest(" Lamen e Macarrões ");

        when(catalogoRepository.existsByNomeIgnoreCase(
                "Lamen e Macarrões"
        )).thenReturn(false);

        when(catalogoRepository.save(any(Catalogo.class)))
                .thenAnswer(invocation -> {

                    Catalogo catalogoSalvo =
                            invocation.getArgument(0);

                    catalogoSalvo.setId(1L);

                    return catalogoSalvo;
                });

        CatalogoResponse resultado =
                catalogoService.cadastrar(requestComEspacos);

        assertEquals(1L, resultado.id());

        assertEquals(
                "Lamen e Macarrões",
                resultado.nome()
        );

        verify(catalogoRepository)
                .existsByNomeIgnoreCase("Lamen e Macarrões");

        verify(catalogoRepository).save(argThat(
                catalogoSalvo -> catalogoSalvo.getNome()
                        .equals("Lamen e Macarrões")
        ));
    }

    // =========================
    // LISTAR TODOS
    // =========================

    @Test
    void deveListarTodosOsCatalogos() {

        Catalogo segundoCatalogo = new Catalogo();
        segundoCatalogo.setId(2L);
        segundoCatalogo.setNome("Snacks e Doces");

        when(catalogoRepository.findAll())
                .thenReturn(List.of(
                        catalogo,
                        segundoCatalogo
                ));

        List<CatalogoResponse> resultado =
                catalogoService.listarTodos();

        assertEquals(2, resultado.size());

        assertEquals(
                List.of(
                        response,
                        new CatalogoResponse(2L, "Snacks e Doces")
                ),
                resultado
        );

        verify(catalogoRepository).findAll();
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremCatalogos() {

        when(catalogoRepository.findAll())
                .thenReturn(List.of());

        List<CatalogoResponse> resultado =
                catalogoService.listarTodos();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verify(catalogoRepository).findAll();
    }

    // =========================
    // BUSCAR POR ID
    // =========================

    @Test
    void deveBuscarCatalogoPorId() {

        when(catalogoRepository.findById(1L))
                .thenReturn(Optional.of(catalogo));

        CatalogoResponse resultado =
                catalogoService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals(response, resultado);

        verify(catalogoRepository).findById(1L);
    }

    @Test
    void deveLancarExcecaoAoBuscarCatalogoInexistente() {

        when(catalogoRepository.findById(99L))
                .thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> catalogoService.buscarPorId(99L)
        );

        assertEquals(
                "Catálogo não encontrado.",
                exception.getMessage()
        );

        verify(catalogoRepository).findById(99L);
    }

    // =========================
    // ATUALIZAR
    // =========================

    @Test
    void deveAtualizarCatalogoComSucesso() {

        CatalogoRequest requestAtualizacao =
                new CatalogoRequest(" Lamen e Massas ");

        when(catalogoRepository.findById(1L))
                .thenReturn(Optional.of(catalogo));

        when(catalogoRepository.existsByNomeIgnoreCaseAndIdNot(
                "Lamen e Massas",
                1L
        )).thenReturn(false);

        when(catalogoRepository.save(catalogo))
                .thenReturn(catalogo);

        CatalogoResponse resultado =
                catalogoService.atualizar(1L, requestAtualizacao);

        assertNotNull(resultado);
        assertEquals(1L, resultado.id());

        assertEquals(
                "Lamen e Massas",
                resultado.nome()
        );

        assertEquals(
                "Lamen e Massas",
                catalogo.getNome()
        );

        verify(catalogoRepository)
                .existsByNomeIgnoreCaseAndIdNot(
                        "Lamen e Massas",
                        1L
                );

        verify(catalogoRepository).save(catalogo);
    }

    @Test
    void deveAtualizarCatalogoMantendoProprioNome() {

        when(catalogoRepository.findById(1L))
                .thenReturn(Optional.of(catalogo));

        when(catalogoRepository.existsByNomeIgnoreCaseAndIdNot(
                request.nome(),
                1L
        )).thenReturn(false);

        when(catalogoRepository.save(catalogo))
                .thenReturn(catalogo);

        CatalogoResponse resultado =
                catalogoService.atualizar(1L, request);

        assertEquals(response, resultado);

        verify(catalogoRepository)
                .existsByNomeIgnoreCaseAndIdNot(
                        request.nome(),
                        1L
                );

        verify(catalogoRepository).save(catalogo);
    }

    @Test
    void deveLancarExcecaoAoAtualizarComNomeDuplicado() {

        CatalogoRequest requestDuplicado =
                new CatalogoRequest("Snacks e Doces");

        when(catalogoRepository.findById(1L))
                .thenReturn(Optional.of(catalogo));

        when(catalogoRepository.existsByNomeIgnoreCaseAndIdNot(
                "Snacks e Doces",
                1L
        )).thenReturn(true);

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> catalogoService.atualizar(
                        1L,
                        requestDuplicado
                )
        );

        assertEquals(
                "Já existe um catálogo cadastrado com este nome.",
                exception.getMessage()
        );

        assertEquals(
                "Lamen e Macarrões",
                catalogo.getNome()
        );

        verify(catalogoRepository, never())
                .save(any(Catalogo.class));
    }

    @Test
    void deveLancarExcecaoAoAtualizarCatalogoInexistente() {

        when(catalogoRepository.findById(99L))
                .thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> catalogoService.atualizar(99L, request)
        );

        assertEquals(
                "Catálogo não encontrado.",
                exception.getMessage()
        );

        verify(catalogoRepository, never())
                .existsByNomeIgnoreCaseAndIdNot(
                        anyString(),
                        anyLong()
                );

        verify(catalogoRepository, never())
                .save(any(Catalogo.class));
    }

    // =========================
    // EXCLUIR
    // =========================

    @Test
    void deveExcluirCatalogoComSucesso() {

        when(catalogoRepository.findById(1L))
                .thenReturn(Optional.of(catalogo));

        when(produtoRepository.existsByCatalogoId(1L))
                .thenReturn(false);

        catalogoService.excluir(1L);

        verify(catalogoRepository).findById(1L);

        verify(produtoRepository).existsByCatalogoId(1L);

        verify(catalogoRepository).delete(catalogo);
    }

    @Test
    void deveLancarExcecaoAoExcluirCatalogoInexistente() {

        when(catalogoRepository.findById(99L))
                .thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> catalogoService.excluir(99L)
        );

        assertEquals(
                "Catálogo não encontrado.",
                exception.getMessage()
        );

        verify(catalogoRepository, never())
                .delete(any(Catalogo.class));

        verifyNoInteractions(produtoRepository);
    }

    @Test
    void deveLancarExcecaoAoExcluirCatalogoComProdutosAssociados() {

        when(catalogoRepository.findById(1L))
                .thenReturn(Optional.of(catalogo));

        when(produtoRepository.existsByCatalogoId(1L))
                .thenReturn(true);

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> catalogoService.excluir(1L)
        );

        assertEquals(
                "Não é possível excluir o catálogo, pois existem produtos associados!",
                exception.getMessage()
        );

        verify(produtoRepository).existsByCatalogoId(1L);

        verify(catalogoRepository, never())
                .delete(any(Catalogo.class));
    }
}
