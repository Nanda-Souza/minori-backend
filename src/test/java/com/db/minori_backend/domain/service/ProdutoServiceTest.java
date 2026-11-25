package com.db.minori_backend.domain.service;

import com.db.minori_backend.domain.dto.ProdutoRequest;
import com.db.minori_backend.domain.dto.ProdutoResponse;
import com.db.minori_backend.domain.entity.*;
import com.db.minori_backend.domain.exception.RecursoNaoEncontradoException;
import com.db.minori_backend.domain.exception.RegraNegocioException;
import com.db.minori_backend.domain.mapper.ProdutoMapper;
import com.db.minori_backend.domain.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private CatalogoRepository catalogoRepository;

    @Mock
    private MarcaRepository marcaRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ProdutoMapper produtoMapper;

    @InjectMocks
    private ProdutoService produtoService;

    private Produto produto;
    private Catalogo catalogo;
    private Marca marca;
    private Categoria categoria;

    private ProdutoRequest request;
    private ProdutoResponse response;

    @BeforeEach
    void setUp() {

        catalogo = new Catalogo();
        catalogo.setId(1L);
        catalogo.setNome("Lamen e Macarrões");

        marca = new Marca();
        marca.setId(1L);
        marca.setNome("Nissin");

        categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNome("Macarrão instantâneo");

        produto = new Produto();
        produto.setId(1L);
        produto.setNome("Lamen Shoyu");
        produto.setPreco(new BigDecimal("15.90"));
        produto.setQuantidade(10);
        produto.setDescricao("Macarrão instantâneo sabor shoyu.");
        produto.setImagemUrl("https://exemplo.com/lamen.jpg");
        produto.setCatalogo(catalogo);
        produto.setMarca(marca);
        produto.setCategoria(categoria);

        request = new ProdutoRequest(
                " Lamen Shoyu ",
                new BigDecimal("15.90"),
                10,
                "Macarrão instantâneo sabor shoyu.",
                "https://exemplo.com/lamen.jpg",
                1L,
                1L,
                1L
        );

        response = new ProdutoResponse(
                1L,
                "Lamen Shoyu",
                new BigDecimal("15.90"),
                10,
                "Macarrão instantâneo sabor shoyu.",
                "https://exemplo.com/lamen.jpg",
                1L,
                "Lamen e Macarrões",
                1L,
                "Nissin",
                1L,
                "Macarrão instantâneo"
        );
    }

    private void simularRelacionamentosExistentes() {

        when(catalogoRepository.findById(1L))
                .thenReturn(Optional.of(catalogo));

        when(marcaRepository.findById(1L))
                .thenReturn(Optional.of(marca));

        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));
    }

    // =========================
    // CADASTRAR
    // =========================

    @Test
    void deveCadastrarProdutoComSucesso() {

        when(produtoRepository.existsByNomeIgnoreCase("Lamen Shoyu"))
                .thenReturn(false);

        simularRelacionamentosExistentes();

        when(produtoRepository.save(any(Produto.class)))
                .thenReturn(produto);

        when(produtoMapper.toResponse(produto))
                .thenReturn(response);

        ProdutoResponse resultado = produtoService.cadastrar(request);

        assertNotNull(resultado);
        assertEquals(response, resultado);

        verify(produtoRepository).existsByNomeIgnoreCase("Lamen Shoyu");

        verify(produtoRepository).save(argThat(p ->
                p.getNome().equals("Lamen Shoyu")
                        && p.getPreco().equals(request.preco())
                        && p.getQuantidade().equals(request.quantidade())
                        && p.getDescricao().equals(request.descricao())
                        && p.getImagemUrl().equals(request.imagemUrl())
                        && p.getCatalogo() == catalogo
                        && p.getMarca() == marca
                        && p.getCategoria() == categoria
        ));

        verify(produtoMapper).toResponse(produto);
    }

    @Test
    void deveLancarExcecaoAoCadastrarProdutoDuplicado() {

        when(produtoRepository.existsByNomeIgnoreCase("Lamen Shoyu"))
                .thenReturn(true);

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> produtoService.cadastrar(request)
        );

        assertEquals(
                "Já existe um produto cadastrado com este nome.",
                exception.getMessage()
        );

        verify(produtoRepository, never()).save(any(Produto.class));

        verifyNoInteractions(
                catalogoRepository,
                marcaRepository,
                categoriaRepository,
                produtoMapper
        );
    }

    @Test
    void deveLancarExcecaoAoCadastrarComCatalogoInexistente() {

        when(catalogoRepository.findById(1L))
                .thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> produtoService.cadastrar(request)
        );

        assertEquals("Catálogo não encontrado.", exception.getMessage());

        verify(produtoRepository, never()).save(any(Produto.class));
    }

    @Test
    void deveLancarExcecaoAoCadastrarComMarcaInexistente() {

        when(catalogoRepository.findById(1L))
                .thenReturn(Optional.of(catalogo));

        when(marcaRepository.findById(1L))
                .thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> produtoService.cadastrar(request)
        );

        assertEquals("Marca não encontrada.", exception.getMessage());

        verify(produtoRepository, never()).save(any(Produto.class));
    }

    @Test
    void deveLancarExcecaoAoCadastrarComCategoriaInexistente() {

        when(catalogoRepository.findById(1L))
                .thenReturn(Optional.of(catalogo));

        when(marcaRepository.findById(1L))
                .thenReturn(Optional.of(marca));

        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> produtoService.cadastrar(request)
        );

        assertEquals("Categoria não encontrada.", exception.getMessage());

        verify(produtoRepository, never()).save(any(Produto.class));
    }

    // =========================
    // LISTAR TODOS
    // =========================

    @Test
    void deveListarTodosOsProdutos() {

        when(produtoRepository.findAll())
                .thenReturn(List.of(produto));

        when(produtoMapper.toResponse(produto))
                .thenReturn(response);

        List<ProdutoResponse> resultado = produtoService.listarTodos();

        assertEquals(1, resultado.size());
        assertEquals(response, resultado.getFirst());

        verify(produtoRepository).findAll();
        verify(produtoMapper).toResponse(produto);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremProdutos() {

        when(produtoRepository.findAll())
                .thenReturn(List.of());

        List<ProdutoResponse> resultado = produtoService.listarTodos();

        assertTrue(resultado.isEmpty());

        verify(produtoRepository).findAll();
        verifyNoInteractions(produtoMapper);
    }

    // =========================
    // BUSCAR POR ID
    // =========================

    @Test
    void deveBuscarProdutoPorId() {

        when(produtoRepository.findById(1L))
                .thenReturn(Optional.of(produto));

        when(produtoMapper.toResponse(produto))
                .thenReturn(response);

        ProdutoResponse resultado = produtoService.buscarPorId(1L);

        assertEquals(response, resultado);

        verify(produtoRepository).findById(1L);
    }

    @Test
    void deveLancarExcecaoAoBuscarProdutoInexistente() {

        when(produtoRepository.findById(99L))
                .thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> produtoService.buscarPorId(99L)
        );

        assertEquals("Produto não encontrado.", exception.getMessage());

        verifyNoInteractions(produtoMapper);
    }

    // =========================
    // ATUALIZAR
    // =========================

    @Test
    void deveAtualizarProdutoComSucesso() {

        ProdutoRequest requestAtualizado = new ProdutoRequest(
                " Lamen Shoyu Premium ",
                new BigDecimal("19.90"),
                20,
                "Nova descrição.",
                "https://exemplo.com/premium.jpg",
                1L,
                1L,
                1L
        );

        ProdutoResponse responseAtualizado = new ProdutoResponse(
                1L,
                "Lamen Shoyu Premium",
                new BigDecimal("19.90"),
                20,
                "Nova descrição.",
                "https://exemplo.com/premium.jpg",
                1L,
                catalogo.getNome(),
                1L,
                marca.getNome(),
                1L,
                categoria.getNome()
        );

        when(produtoRepository.findById(1L))
                .thenReturn(Optional.of(produto));

        when(produtoRepository.existsByNomeIgnoreCaseAndIdNot(
                "Lamen Shoyu Premium", 1L))
                .thenReturn(false);

        simularRelacionamentosExistentes();

        when(produtoRepository.save(produto))
                .thenReturn(produto);

        when(produtoMapper.toResponse(produto))
                .thenReturn(responseAtualizado);

        ProdutoResponse resultado = produtoService.atualizar(
                1L, requestAtualizado
        );

        assertEquals(responseAtualizado, resultado);
        assertEquals("Lamen Shoyu Premium", produto.getNome());
        assertEquals(new BigDecimal("19.90"), produto.getPreco());
        assertEquals(20, produto.getQuantidade());
        assertEquals("Nova descrição.", produto.getDescricao());
        assertEquals(requestAtualizado.imagemUrl(), produto.getImagemUrl());

        verify(produtoRepository)
                .existsByNomeIgnoreCaseAndIdNot("Lamen Shoyu Premium", 1L);

        verify(produtoRepository).save(produto);
    }

    @Test
    void deveLancarExcecaoAoAtualizarComNomeDuplicado() {

        when(produtoRepository.findById(1L))
                .thenReturn(Optional.of(produto));

        when(produtoRepository.existsByNomeIgnoreCaseAndIdNot(
                "Lamen Shoyu", 1L))
                .thenReturn(true);

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> produtoService.atualizar(1L, request)
        );

        assertEquals(
                "Já existe um produto cadastrado com este nome.",
                exception.getMessage()
        );

        verify(produtoRepository, never()).save(any(Produto.class));
    }

    @Test
    void deveLancarExcecaoAoAtualizarProdutoInexistente() {

        when(produtoRepository.findById(99L))
                .thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> produtoService.atualizar(99L, request)
        );

        assertEquals("Produto não encontrado.", exception.getMessage());

        verify(produtoRepository, never()).save(any(Produto.class));
    }

    @Test
    void deveLancarExcecaoAoAtualizarComCatalogoInexistente() {

        when(produtoRepository.findById(1L))
                .thenReturn(Optional.of(produto));

        when(catalogoRepository.findById(1L))
                .thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> produtoService.atualizar(1L, request)
        );

        assertEquals("Catálogo não encontrado.", exception.getMessage());

        verify(produtoRepository, never()).save(any(Produto.class));
    }

    @Test
    void deveLancarExcecaoAoAtualizarComMarcaInexistente() {

        when(produtoRepository.findById(1L))
                .thenReturn(Optional.of(produto));

        when(catalogoRepository.findById(1L))
                .thenReturn(Optional.of(catalogo));

        when(marcaRepository.findById(1L))
                .thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> produtoService.atualizar(1L, request)
        );

        assertEquals("Marca não encontrada.", exception.getMessage());

        verify(produtoRepository, never()).save(any(Produto.class));
    }

    @Test
    void deveLancarExcecaoAoAtualizarComCategoriaInexistente() {

        when(produtoRepository.findById(1L))
                .thenReturn(Optional.of(produto));

        when(catalogoRepository.findById(1L))
                .thenReturn(Optional.of(catalogo));

        when(marcaRepository.findById(1L))
                .thenReturn(Optional.of(marca));

        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> produtoService.atualizar(1L, request)
        );

        assertEquals("Categoria não encontrada.", exception.getMessage());

        verify(produtoRepository, never()).save(any(Produto.class));
    }

    // =========================
    // EXCLUIR
    // =========================

    @Test
    void deveExcluirProdutoComSucesso() {

        when(produtoRepository.findById(1L))
                .thenReturn(Optional.of(produto));

        assertDoesNotThrow(() -> produtoService.excluir(1L));

        verify(produtoRepository).findById(1L);
        verify(produtoRepository).delete(produto);
    }

    @Test
    void deveLancarExcecaoAoExcluirProdutoInexistente() {

        when(produtoRepository.findById(99L))
                .thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> produtoService.excluir(99L)
        );

        assertEquals("Produto não encontrado.", exception.getMessage());

        verify(produtoRepository, never()).delete(any(Produto.class));
    }

    // =========================
    // FILTRAR
    // =========================

    @Test
    void deveFiltrarProdutosComSucesso() {

        when(produtoRepository.findAll(
                any(Specification.class)))
                .thenReturn(List.of(produto));

        when(produtoMapper.toResponse(produto))
                .thenReturn(response);

        List<ProdutoResponse> resultado = produtoService.filtrar(
                1L,
                1L,
                1L,
                new BigDecimal("10.00"),
                new BigDecimal("20.00")
        );

        assertEquals(1, resultado.size());
        assertEquals(response, resultado.getFirst());

        verify(produtoRepository).findAll(any(Specification.class));
    }

    @Test
    void deveConsultarProdutosSemFiltros() {

        when(produtoRepository.findAll(
                any(Specification.class)))
                .thenReturn(List.of(produto));

        when(produtoMapper.toResponse(produto))
                .thenReturn(response);

        List<ProdutoResponse> resultado = produtoService.filtrar(
                null, null, null, null, null
        );

        assertEquals(1, resultado.size());
        assertEquals(response, resultado.getFirst());

        verify(produtoRepository).findAll(any(Specification.class));
    }

    @Test
    void deveLancarExcecaoQuandoPrecoMinimoForNegativo() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> produtoService.filtrar(
                        null,
                        null,
                        null,
                        new BigDecimal("-10.00"),
                        null
                )
        );

        assertEquals(
                "O preço mínimo não pode ser negativo.",
                exception.getMessage()
        );

        verifyNoInteractions(produtoRepository);
    }

    @Test
    void deveLancarExcecaoQuandoPrecoMaximoForNegativo() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> produtoService.filtrar(
                        null,
                        null,
                        null,
                        null,
                        new BigDecimal("-20.00")
                )
        );

        assertEquals(
                "O preço máximo não pode ser negativo.",
                exception.getMessage()
        );

        verifyNoInteractions(produtoRepository);
    }

    @Test
    void deveLancarExcecaoQuandoPrecoMinimoForMaiorQueMaximo() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> produtoService.filtrar(
                        null,
                        null,
                        null,
                        new BigDecimal("50.00"),
                        new BigDecimal("10.00")
                )
        );

        assertEquals(
                "O preço mínimo não pode ser maior que o preço máximo.",
                exception.getMessage()
        );

        verifyNoInteractions(produtoRepository);
    }
}
