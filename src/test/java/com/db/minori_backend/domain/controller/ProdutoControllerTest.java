
package com.db.minori_backend.domain.controller;

import com.db.minori_backend.domain.entity.*;
import com.db.minori_backend.domain.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProdutoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private CatalogoRepository catalogoRepository;

    @Autowired
    private MarcaRepository marcaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private Catalogo catalogo;
    private Marca marca;
    private Categoria categoria;

    @BeforeEach
    void setUp() {

        catalogo = new Catalogo();
        catalogo.setNome("Lamen e Macarrões");
        catalogo = catalogoRepository.save(catalogo);

        marca = new Marca();
        marca.setNome("Nissin");
        marca = marcaRepository.save(marca);

        categoria = new Categoria();
        categoria.setNome("Macarrão instantâneo");
        categoria = categoriaRepository.save(categoria);
    }

    private Produto criarProduto(String nome, BigDecimal preco) {

        Produto produto = new Produto();

        produto.setNome(nome);
        produto.setPreco(preco);
        produto.setQuantidade(10);
        produto.setDescricao("Descrição do produto.");
        produto.setImagemUrl("https://exemplo.com/produto.jpg");

        produto.setCatalogo(catalogo);
        produto.setMarca(marca);
        produto.setCategoria(categoria);

        return produtoRepository.saveAndFlush(produto);
    }

    private String criarJson(
            String nome,
            BigDecimal preco,
            Integer quantidade,
            String descricao,
            Long catalogoId,
            Long marcaId,
            Long categoriaId
    ) {

        return """
                {
                    "nome": "%s",
                    "preco": %s,
                    "quantidade": %d,
                    "descricao": "%s",
                    "imagemUrl": "https://exemplo.com/produto.jpg",
                    "catalogoId": %d,
                    "marcaId": %d,
                    "categoriaId": %d
                }
                """.formatted(
                nome,
                preco,
                quantidade,
                descricao,
                catalogoId,
                marcaId,
                categoriaId
        );
    }

    private String criarJsonValido(String nome) {

        return criarJson(
                nome,
                new BigDecimal("15.90"),
                10,
                "Macarrão instantâneo sabor shoyu.",
                catalogo.getId(),
                marca.getId(),
                categoria.getId()
        );
    }

    // =========================
    // CADASTRAR
    // =========================

    @Test
    void deveCadastrarProdutoComSucesso() throws Exception {

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJsonValido(" Lamen Shoyu ")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Lamen Shoyu"))
                .andExpect(jsonPath("$.preco").value(15.90))
                .andExpect(jsonPath("$.quantidade").value(10))
                .andExpect(jsonPath("$.catalogoId").value(catalogo.getId()))
                .andExpect(jsonPath("$.catalogoNome").value(catalogo.getNome()))
                .andExpect(jsonPath("$.marcaId").value(marca.getId()))
                .andExpect(jsonPath("$.marcaNome").value(marca.getNome()))
                .andExpect(jsonPath("$.categoriaId").value(categoria.getId()))
                .andExpect(jsonPath("$.categoriaNome").value(categoria.getNome()));

        assertTrue(
                produtoRepository.existsByNomeIgnoreCase("Lamen Shoyu")
        );
    }

    @Test
    void deveRetornar409AoCadastrarProdutoDuplicado() throws Exception {

        criarProduto("Lamen Shoyu", new BigDecimal("15.90"));

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJsonValido("Lamen Shoyu")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensagem").value(
                        "Já existe um produto cadastrado com este nome."
                ))
                .andExpect(jsonPath("$.caminho").value("/produtos"));
    }

    @Test
    void deveRetornar409AoCadastrarNomeDuplicadoEmMaiusculas()
            throws Exception {

        criarProduto("Lamen Shoyu", new BigDecimal("15.90"));

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJsonValido("LAMEN SHOYU")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void deveRetornar409AoCadastrarNomeDuplicadoComEspacos()
            throws Exception {

        criarProduto("Lamen Shoyu", new BigDecimal("15.90"));

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJsonValido(" Lamen Shoyu ")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void deveRetornar400AoCadastrarProdutoSemNome() throws Exception {

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJsonValido("")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value(
                        "Existem campos inválidos na requisição."
                ))
                .andExpect(jsonPath("$.campos.nome").value(
                        "O nome é obrigatório."
                ));
    }

    @Test
    void deveRetornar400QuandoNomeUltrapassar200Caracteres()
            throws Exception {

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJsonValido("A".repeat(201))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists());
    }

    @Test
    void deveRetornar400AoCadastrarProdutoSemDescricao()
            throws Exception {

        String json = criarJson(
                "Lamen Shoyu",
                new BigDecimal("15.90"),
                10,
                "",
                catalogo.getId(),
                marca.getId(),
                categoria.getId()
        );

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.descricao").exists());
    }

    @Test
    void deveRetornar400AoCadastrarProdutoComPrecoInvalido()
            throws Exception {

        String json = criarJson(
                "Lamen Shoyu",
                BigDecimal.ZERO,
                10,
                "Descrição do produto.",
                catalogo.getId(),
                marca.getId(),
                categoria.getId()
        );

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.preco").value(
                        "O preço deve ser maior que zero."
                ));
    }

    @Test
    void deveRetornar400AoCadastrarProdutoComQuantidadeNegativa()
            throws Exception {

        String json = criarJson(
                "Lamen Shoyu",
                new BigDecimal("15.90"),
                -1,
                "Descrição do produto.",
                catalogo.getId(),
                marca.getId(),
                categoria.getId()
        );

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.quantidade").value(
                        "A quantidade não pode ser negativa."
                ));
    }

    @Test
    void deveRetornar404AoCadastrarComCatalogoInexistente()
            throws Exception {

        String json = criarJson(
                "Lamen Shoyu",
                new BigDecimal("15.90"),
                10,
                "Descrição do produto.",
                Long.MAX_VALUE,
                marca.getId(),
                categoria.getId()
        );

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value(
                        "Catálogo não encontrado."
                ));
    }

    @Test
    void deveRetornar404AoCadastrarComMarcaInexistente()
            throws Exception {

        String json = criarJson(
                "Lamen Shoyu",
                new BigDecimal("15.90"),
                10,
                "Descrição do produto.",
                catalogo.getId(),
                Long.MAX_VALUE,
                categoria.getId()
        );

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value(
                        "Marca não encontrada."
                ));
    }

    @Test
    void deveRetornar404AoCadastrarComCategoriaInexistente()
            throws Exception {

        String json = criarJson(
                "Lamen Shoyu",
                new BigDecimal("15.90"),
                10,
                "Descrição do produto.",
                catalogo.getId(),
                marca.getId(),
                Long.MAX_VALUE
        );

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value(
                        "Categoria não encontrada."
                ));
    }

    @Test
    void deveRetornar400QuandoJsonForInvalido() throws Exception {

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(
                        "O corpo da requisição está inválido ou malformado."
                ));
    }

    // =========================
    // LISTAR TODOS
    // =========================

    @Test
    void deveListarTodosOsProdutos() throws Exception {

        criarProduto("Lamen Shoyu", new BigDecimal("15.90"));
        criarProduto("Lamen Miso", new BigDecimal("19.90"));

        mockMvc.perform(get("/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremProdutos()
            throws Exception {

        mockMvc.perform(get("/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // =========================
    // BUSCAR POR ID
    // =========================

    @Test
    void deveBuscarProdutoPorId() throws Exception {

        Produto produto = criarProduto(
                "Lamen Shoyu",
                new BigDecimal("15.90")
        );

        mockMvc.perform(get("/produtos/{id}", produto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(produto.getId()))
                .andExpect(jsonPath("$.nome").value("Lamen Shoyu"))
                .andExpect(jsonPath("$.preco").value(15.90));
    }

    @Test
    void deveRetornar404AoBuscarProdutoInexistente() throws Exception {

        mockMvc.perform(get("/produtos/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value(
                        "Produto não encontrado."
                ));
    }

    // =========================
    // ATUALIZAR
    // =========================

    @Test
    void deveAtualizarProdutoComSucesso() throws Exception {

        Produto produto = criarProduto(
                "Lamen Shoyu",
                new BigDecimal("15.90")
        );

        String json = criarJson(
                " Lamen Shoyu Premium ",
                new BigDecimal("25.90"),
                20,
                "Descrição atualizada.",
                catalogo.getId(),
                marca.getId(),
                categoria.getId()
        );

        mockMvc.perform(put("/produtos/{id}", produto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(produto.getId()))
                .andExpect(jsonPath("$.nome").value("Lamen Shoyu Premium"))
                .andExpect(jsonPath("$.preco").value(25.90))
                .andExpect(jsonPath("$.quantidade").value(20))
                .andExpect(jsonPath("$.descricao").value(
                        "Descrição atualizada."
                ));

        produtoRepository.flush();

        assertTrue(
                produtoRepository.existsByNomeIgnoreCase(
                        "Lamen Shoyu Premium"
                )
        );
    }

    @Test
    void deveAtualizarProdutoMantendoProprioNome() throws Exception {

        Produto produto = criarProduto(
                "Lamen Shoyu",
                new BigDecimal("15.90")
        );

        mockMvc.perform(put("/produtos/{id}", produto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJsonValido("Lamen Shoyu")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Lamen Shoyu"));
    }

    @Test
    void deveRetornar409AoAtualizarComNomeDeOutroProduto()
            throws Exception {

        Produto produto = criarProduto(
                "Lamen Shoyu",
                new BigDecimal("15.90")
        );

        criarProduto("Lamen Miso", new BigDecimal("19.90"));

        mockMvc.perform(put("/produtos/{id}", produto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJsonValido("LAMEN MISO")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value(
                        "Já existe um produto cadastrado com este nome."
                ));
    }

    @Test
    void deveRetornar404AoAtualizarProdutoInexistente()
            throws Exception {

        mockMvc.perform(put("/produtos/{id}", Long.MAX_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJsonValido("Lamen Shoyu")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value(
                        "Produto não encontrado."
                ));
    }

    @Test
    void deveRetornar400AoAtualizarComNomeInvalido()
            throws Exception {

        Produto produto = criarProduto(
                "Lamen Shoyu",
                new BigDecimal("15.90")
        );

        mockMvc.perform(put("/produtos/{id}", produto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJsonValido("")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists());
    }

    // =========================
    // EXCLUIR
    // =========================

    @Test
    void deveExcluirProdutoComSucesso() throws Exception {

        Produto produto = criarProduto(
                "Lamen Shoyu",
                new BigDecimal("15.90")
        );

        mockMvc.perform(delete("/produtos/{id}", produto.getId()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        produtoRepository.flush();

        assertFalse(produtoRepository.existsById(produto.getId()));
    }

    @Test
    void deveRetornar404AoExcluirProdutoInexistente()
            throws Exception {

        mockMvc.perform(delete("/produtos/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value(
                        "Produto não encontrado."
                ));
    }

    // =========================
    // FILTRAR
    // =========================

    @Test
    void deveFiltrarProdutosPorCatalogo() throws Exception {

        criarProduto("Lamen Shoyu", new BigDecimal("15.90"));

        Catalogo outroCatalogo = new Catalogo();
        outroCatalogo.setNome("Snacks e Doces");
        outroCatalogo = catalogoRepository.save(outroCatalogo);

        Produto outroProduto = criarProduto(
                "Snack de arroz",
                new BigDecimal("10.00")
        );

        outroProduto.setCatalogo(outroCatalogo);
        produtoRepository.saveAndFlush(outroProduto);

        mockMvc.perform(get("/produtos/filtrar")
                        .param("catalogoId", catalogo.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Lamen Shoyu"));
    }

    @Test
    void deveFiltrarProdutosPorMarca() throws Exception {

        criarProduto("Lamen Shoyu", new BigDecimal("15.90"));

        Marca outraMarca = new Marca();
        outraMarca.setNome("Samyang");
        outraMarca = marcaRepository.save(outraMarca);

        Produto outroProduto = criarProduto(
                "Lamen Picante",
                new BigDecimal("25.90")
        );

        outroProduto.setMarca(outraMarca);
        produtoRepository.saveAndFlush(outroProduto);

        mockMvc.perform(get("/produtos/filtrar")
                        .param("marcaId", marca.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Lamen Shoyu"));
    }

    @Test
    void deveFiltrarProdutosPorCategoria() throws Exception {

        criarProduto("Lamen Shoyu", new BigDecimal("15.90"));

        Categoria outraCategoria = new Categoria();
        outraCategoria.setNome("Macarrão de arroz");
        outraCategoria = categoriaRepository.save(outraCategoria);

        Produto outroProduto = criarProduto(
                "Macarrão de arroz",
                new BigDecimal("20.00")
        );

        outroProduto.setCategoria(outraCategoria);
        produtoRepository.saveAndFlush(outroProduto);

        mockMvc.perform(get("/produtos/filtrar")
                        .param("categoriaId", categoria.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Lamen Shoyu"));
    }

    @Test
    void deveFiltrarProdutosPorFaixaDePreco() throws Exception {

        criarProduto("Lamen Shoyu", new BigDecimal("15.90"));
        criarProduto("Lamen Premium", new BigDecimal("35.90"));
        criarProduto("Lamen Simples", new BigDecimal("5.90"));

        mockMvc.perform(get("/produtos/filtrar")
                        .param("precoMin", "10.00")
                        .param("precoMax", "20.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Lamen Shoyu"));
    }

    @Test
    void deveFiltrarIncluindoLimitesDePreco() throws Exception {

        criarProduto("Lamen Shoyu", new BigDecimal("10.00"));
        criarProduto("Lamen Miso", new BigDecimal("20.00"));
        criarProduto("Lamen Premium", new BigDecimal("30.00"));

        mockMvc.perform(get("/produtos/filtrar")
                        .param("precoMin", "10.00")
                        .param("precoMax", "20.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void deveCombinarFiltrosDeProduto() throws Exception {

        criarProduto("Lamen Shoyu", new BigDecimal("15.90"));
        criarProduto("Lamen Premium", new BigDecimal("35.90"));

        Marca outraMarca = new Marca();
        outraMarca.setNome("Samyang");
        outraMarca = marcaRepository.save(outraMarca);

        Produto outroProduto = criarProduto(
                "Lamen Picante",
                new BigDecimal("15.90")
        );

        outroProduto.setMarca(outraMarca);
        produtoRepository.saveAndFlush(outroProduto);

        mockMvc.perform(get("/produtos/filtrar")
                        .param("catalogoId", catalogo.getId().toString())
                        .param("marcaId", marca.getId().toString())
                        .param("categoriaId", categoria.getId().toString())
                        .param("precoMin", "10.00")
                        .param("precoMax", "20.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Lamen Shoyu"));
    }

    @Test
    void deveRetornarTodosOsProdutosQuandoNaoHouverFiltros()
            throws Exception {

        criarProduto("Lamen Shoyu", new BigDecimal("15.90"));
        criarProduto("Lamen Miso", new BigDecimal("19.90"));

        mockMvc.perform(get("/produtos/filtrar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void deveRetornarListaVaziaQuandoFiltroNaoEncontrarProdutos()
            throws Exception {

        criarProduto("Lamen Shoyu", new BigDecimal("15.90"));

        mockMvc.perform(get("/produtos/filtrar")
                        .param("precoMin", "100.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deveRetornar400QuandoPrecoMinimoForNegativo()
            throws Exception {

        mockMvc.perform(get("/produtos/filtrar")
                        .param("precoMin", "-10.00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(
                        "O preço mínimo não pode ser negativo."
                ));
    }

    @Test
    void deveRetornar400QuandoPrecoMaximoForNegativo()
            throws Exception {

        mockMvc.perform(get("/produtos/filtrar")
                        .param("precoMax", "-20.00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(
                        "O preço máximo não pode ser negativo."
                ));
    }

    @Test
    void deveRetornar400QuandoPrecoMinimoForMaiorQueMaximo()
            throws Exception {

        mockMvc.perform(get("/produtos/filtrar")
                        .param("precoMin", "50.00")
                        .param("precoMax", "10.00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(
                        "O preço mínimo não pode ser maior que o preço máximo."
                ));
    }
}
