
package com.db.minori_backend.domain.service;

import com.db.minori_backend.domain.dto.MarcaRequest;
import com.db.minori_backend.domain.dto.MarcaResponse;
import com.db.minori_backend.domain.entity.Marca;
import com.db.minori_backend.domain.exception.RecursoNaoEncontradoException;
import com.db.minori_backend.domain.exception.RegraNegocioException;
import com.db.minori_backend.domain.repository.MarcaRepository;
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
class MarcaServiceTest {

    @Mock
    private MarcaRepository marcaRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private MarcaService marcaService;

    private Marca marca;
    private MarcaRequest request;
    private MarcaResponse response;

    @BeforeEach
    void setUp() {

        marca = new Marca();
        marca.setId(1L);
        marca.setNome("Nissin");

        request = new MarcaRequest("Nissin");

        response = new MarcaResponse(
                1L,
                "Nissin"
        );
    }

    // =========================
    // CADASTRAR
    // =========================

    @Test
    void deveCadastrarMarcaComSucesso() {

        when(marcaRepository.existsByNomeIgnoreCase("Nissin"))
                .thenReturn(false);

        when(marcaRepository.save(any(Marca.class)))
                .thenReturn(marca);

        MarcaResponse resultado = marcaService.cadastrar(request);

        assertNotNull(resultado);
        assertEquals(response, resultado);

        verify(marcaRepository)
                .existsByNomeIgnoreCase("Nissin");

        verify(marcaRepository).save(argThat(m ->
                m.getNome().equals("Nissin")
        ));
    }

    @Test
    void deveLancarExcecaoAoCadastrarMarcaDuplicada() {

        when(marcaRepository.existsByNomeIgnoreCase("Nissin"))
                .thenReturn(true);

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> marcaService.cadastrar(request)
        );

        assertEquals(
                "Já existe uma marca cadastrada com este nome.",
                exception.getMessage()
        );

        verify(marcaRepository, never())
                .save(any(Marca.class));
    }

    @Test
    void deveRemoverEspacosAoCadastrarMarca() {

        MarcaRequest requestComEspacos =
                new MarcaRequest(" Nissin ");

        when(marcaRepository.existsByNomeIgnoreCase("Nissin"))
                .thenReturn(false);

        when(marcaRepository.save(any(Marca.class)))
                .thenAnswer(invocation -> {

                    Marca marcaSalva = invocation.getArgument(0);
                    marcaSalva.setId(1L);

                    return marcaSalva;
                });

        MarcaResponse resultado =
                marcaService.cadastrar(requestComEspacos);

        assertEquals("Nissin", resultado.nome());

        verify(marcaRepository)
                .existsByNomeIgnoreCase("Nissin");

        verify(marcaRepository).save(argThat(m ->
                m.getNome().equals("Nissin")
        ));
    }

    // =========================
    // LISTAR TODOS
    // =========================

    @Test
    void deveListarTodasAsMarcas() {

        Marca outraMarca = new Marca();
        outraMarca.setId(2L);
        outraMarca.setNome("Samyang");

        when(marcaRepository.findAll())
                .thenReturn(List.of(marca, outraMarca));

        List<MarcaResponse> resultado =
                marcaService.listarTodos();

        assertEquals(2, resultado.size());

        assertEquals(
                new MarcaResponse(1L, "Nissin"),
                resultado.get(0)
        );

        assertEquals(
                new MarcaResponse(2L, "Samyang"),
                resultado.get(1)
        );

        verify(marcaRepository).findAll();
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremMarcas() {

        when(marcaRepository.findAll())
                .thenReturn(List.of());

        List<MarcaResponse> resultado =
                marcaService.listarTodos();

        assertTrue(resultado.isEmpty());

        verify(marcaRepository).findAll();
    }

    // =========================
    // BUSCAR POR ID
    // =========================

    @Test
    void deveBuscarMarcaPorId() {

        when(marcaRepository.findById(1L))
                .thenReturn(Optional.of(marca));

        MarcaResponse resultado =
                marcaService.buscarPorId(1L);

        assertEquals(response, resultado);

        verify(marcaRepository).findById(1L);
    }

    @Test
    void deveLancarExcecaoAoBuscarMarcaInexistente() {

        when(marcaRepository.findById(99L))
                .thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> marcaService.buscarPorId(99L)
        );

        assertEquals(
                "Marca não encontrada.",
                exception.getMessage()
        );

        verify(marcaRepository).findById(99L);
    }

    // =========================
    // ATUALIZAR
    // =========================

    @Test
    void deveAtualizarMarcaComSucesso() {

        MarcaRequest requestAtualizado =
                new MarcaRequest(" Nissin Foods ");

        when(marcaRepository.findById(1L))
                .thenReturn(Optional.of(marca));

        when(marcaRepository.existsByNomeIgnoreCaseAndIdNot(
                "Nissin Foods", 1L))
                .thenReturn(false);

        when(marcaRepository.save(marca))
                .thenReturn(marca);

        MarcaResponse resultado =
                marcaService.atualizar(1L, requestAtualizado);

        assertEquals(1L, resultado.id());
        assertEquals("Nissin Foods", resultado.nome());
        assertEquals("Nissin Foods", marca.getNome());

        verify(marcaRepository)
                .existsByNomeIgnoreCaseAndIdNot(
                        "Nissin Foods", 1L
                );

        verify(marcaRepository).save(marca);
    }

    @Test
    void deveAtualizarMarcaMantendoProprioNome() {

        when(marcaRepository.findById(1L))
                .thenReturn(Optional.of(marca));

        when(marcaRepository.existsByNomeIgnoreCaseAndIdNot(
                "Nissin", 1L))
                .thenReturn(false);

        when(marcaRepository.save(marca))
                .thenReturn(marca);

        MarcaResponse resultado =
                marcaService.atualizar(1L, request);

        assertEquals(response, resultado);

        verify(marcaRepository)
                .existsByNomeIgnoreCaseAndIdNot("Nissin", 1L);

        verify(marcaRepository).save(marca);
    }

    @Test
    void deveLancarExcecaoAoAtualizarComNomeDuplicado() {

        MarcaRequest requestAtualizado =
                new MarcaRequest("Samyang");

        when(marcaRepository.findById(1L))
                .thenReturn(Optional.of(marca));

        when(marcaRepository.existsByNomeIgnoreCaseAndIdNot(
                "Samyang", 1L))
                .thenReturn(true);

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> marcaService.atualizar(1L, requestAtualizado)
        );

        assertEquals(
                "Já existe uma marca cadastrada com este nome.",
                exception.getMessage()
        );

        assertEquals("Nissin", marca.getNome());

        verify(marcaRepository, never())
                .save(any(Marca.class));
    }

    @Test
    void deveLancarExcecaoAoAtualizarMarcaInexistente() {

        when(marcaRepository.findById(99L))
                .thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> marcaService.atualizar(99L, request)
        );

        assertEquals(
                "Marca não encontrada.",
                exception.getMessage()
        );

        verify(marcaRepository, never())
                .save(any(Marca.class));

        verify(marcaRepository, never())
                .existsByNomeIgnoreCaseAndIdNot(
                        anyString(), anyLong()
                );
    }

    // =========================
    // EXCLUIR
    // =========================

    @Test
    void deveExcluirMarcaComSucesso() {

        when(marcaRepository.findById(1L))
                .thenReturn(Optional.of(marca));

        when(produtoRepository.existsByMarcaId(1L))
                .thenReturn(false);

        assertDoesNotThrow(
                () -> marcaService.excluir(1L)
        );

        verify(marcaRepository).findById(1L);
        verify(produtoRepository).existsByMarcaId(1L);
        verify(marcaRepository).delete(marca);
    }

    @Test
    void deveLancarExcecaoAoExcluirMarcaInexistente() {

        when(marcaRepository.findById(99L))
                .thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> marcaService.excluir(99L)
        );

        assertEquals(
                "Marca não encontrada.",
                exception.getMessage()
        );

        verify(marcaRepository, never())
                .delete(any(Marca.class));

        verifyNoInteractions(produtoRepository);
    }

    @Test
    void deveLancarExcecaoAoExcluirMarcaComProdutosAssociados() {

        when(marcaRepository.findById(1L))
                .thenReturn(Optional.of(marca));

        when(produtoRepository.existsByMarcaId(1L))
                .thenReturn(true);

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> marcaService.excluir(1L)
        );

        assertEquals(
                "Não é possível excluir a marca, pois existem produtos associados!",
                exception.getMessage()
        );

        verify(produtoRepository).existsByMarcaId(1L);

        verify(marcaRepository, never())
                .delete(any(Marca.class));
    }
}
