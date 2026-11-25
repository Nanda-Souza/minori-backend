
package com.db.minori_backend.domain.controller;

import com.db.minori_backend.domain.entity.*;
import com.db.minori_backend.domain.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CatalogoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CatalogoRepository catalogoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private MarcaRepository marcaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private Catalogo criarCatalogo(String nome) {

        Catalogo catalogo = new Catalogo();
        catalogo.setNome(nome);

        return catalogoRepository.saveAndFlush(catalogo);
    }

    private String criarJson(String nome) {

        return """
                {
                    "nome": "%s"
                }
                """.formatted(nome);
    }

    private Produto criarProdutoAssociado(Catalogo catalogo) {

        Marca marca = new Marca();
        marca.setNome("Nissin");
        marca = marcaRepository.save(marca);

        Categoria categoria = new Categoria();
        categoria.setNome("Macarrão instantâneo");
        categoria = categoriaRepository.save(categoria);

        Produto produto = new Produto();

        produto.setNome("Lamen Shoyu");
        produto.setPreco(new BigDecimal("15.90"));
        produto.setQuantidade(10);
        produto.setDescricao("Macarrão instantâneo sabor shoyu.");
        produto.setImagemUrl("https://exemplo.com/lamen.jpg");

        produto.setCatalogo(catalogo);
        produto.setMarca(marca);
        produto.setCategoria(categoria);

        return produtoRepository.saveAndFlush(produto);
    }

    // =========================
    // CADASTRAR
    // =========================

    @Test
    void deveCadastrarCatalogoComSucesso() throws Exception {

        mockMvc.perform(post("/catalogos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJson("Lamen e Macarrões")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value(
                        "Lamen e Macarrões"
                ));

        assertTrue(
                catalogoRepository.existsByNomeIgnoreCase(
                        "Lamen e Macarrões"
                )
        );
    }

    @Test
    void deveCadastrarCatalogoRemovendoEspacos() throws Exception {

        mockMvc.perform(post("/catalogos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJson(" Lamen e Macarrões ")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value(
                        "Lamen e Macarrões"
                ));

        assertTrue(
                catalogoRepository.existsByNomeIgnoreCase(
                        "Lamen e Macarrões"
                )
        );

        assertFalse(
                catalogoRepository.existsByNomeIgnoreCase(
                        " Lamen e Macarrões "
                )
        );
    }

    @Test
    void deveRetornar409AoCadastrarCatalogoDuplicado()
            throws Exception {

        criarCatalogo("Lamen e Macarrões");

        mockMvc.perform(post("/catalogos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJson("Lamen e Macarrões")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.erro").value("Conflict"))
                .andExpect(jsonPath("$.mensagem").value(
                        "Já existe um catálogo cadastrado com este nome."
                ))
                .andExpect(jsonPath("$.caminho").value("/catalogos"))
                .andExpect(jsonPath("$.timestamp").exists());

        assertEquals(1, catalogoRepository.count());
    }

    @Test
    void deveRetornar409AoCadastrarNomeDuplicadoEmMaiusculas()
            throws Exception {

        criarCatalogo("Lamen e Macarrões");

        mockMvc.perform(post("/catalogos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJson("LAMEN E MACARRÕES")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensagem").value(
                        "Já existe um catálogo cadastrado com este nome."
                ));

        assertEquals(1, catalogoRepository.count());
    }

    @Test
    void deveRetornar409AoCadastrarNomeDuplicadoComEspacos()
            throws Exception {

        criarCatalogo("Lamen e Macarrões");

        mockMvc.perform(post("/catalogos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJson(" Lamen e Macarrões ")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensagem").value(
                        "Já existe um catálogo cadastrado com este nome."
                ));

        assertEquals(1, catalogoRepository.count());
    }

    @Test
    void deveRetornar400AoCadastrarCatalogoSemNome()
            throws Exception {

        mockMvc.perform(post("/catalogos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJson("")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value(
                        "Existem campos inválidos na requisição."
                ))
                .andExpect(jsonPath("$.campos.nome").value(
                        "O nome do catálogo é obrigatório."
                ));

        assertEquals(0, catalogoRepository.count());
    }

    @Test
    void deveRetornar400QuandoNomePossuirApenasEspacos()
            throws Exception {

        mockMvc.perform(post("/catalogos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJson("   ")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").value(
                        "O nome do catálogo é obrigatório."
                ));

        assertEquals(0, catalogoRepository.count());
    }

    @Test
    void deveRetornar400QuandoNomeForNulo()
            throws Exception {

        String json = """
                {
                    "nome": null
                }
                """;

        mockMvc.perform(post("/catalogos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").value(
                        "O nome do catálogo é obrigatório."
                ));

        assertEquals(0, catalogoRepository.count());
    }

    @Test
    void deveCadastrarCatalogoCom200Caracteres()
            throws Exception {

        String nome = "A".repeat(200);

        mockMvc.perform(post("/catalogos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJson(nome)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value(nome));

        assertTrue(
                catalogoRepository.existsByNomeIgnoreCase(nome)
        );
    }

    @Test
    void deveRetornar400QuandoNomeUltrapassar200Caracteres()
            throws Exception {

        String nome = "A".repeat(201);

        mockMvc.perform(post("/catalogos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJson(nome)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").value(
                        "O nome deve ter no máximo 200 caracteres."
                ));

        assertEquals(0, catalogoRepository.count());
    }

    @Test
    void deveRetornar400QuandoJsonForInvalido()
            throws Exception {

        mockMvc.perform(post("/catalogos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(
                        "O corpo da requisição está inválido ou malformado."
                ));

        assertEquals(0, catalogoRepository.count());
    }

    // =========================
    // LISTAR TODOS
    // =========================

    @Test
    void deveListarTodosOsCatalogos() throws Exception {

        criarCatalogo("Lamen e Macarrões");
        criarCatalogo("Snacks e Doces");

        mockMvc.perform(get("/catalogos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].nome")
                        .value(containsInAnyOrder(
                                "Lamen e Macarrões",
                                "Snacks e Doces"
                        )));
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremCatalogos()
            throws Exception {

        mockMvc.perform(get("/catalogos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // =========================
    // BUSCAR POR ID
    // =========================

    @Test
    void deveBuscarCatalogoPorId() throws Exception {

        Catalogo catalogo =
                criarCatalogo("Lamen e Macarrões");

        mockMvc.perform(get("/catalogos/{id}", catalogo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(catalogo.getId()))
                .andExpect(jsonPath("$.nome").value(
                        "Lamen e Macarrões"
                ));
    }

    @Test
    void deveRetornar404AoBuscarCatalogoInexistente()
            throws Exception {

        mockMvc.perform(get("/catalogos/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.erro").value("Not Found"))
                .andExpect(jsonPath("$.mensagem").value(
                        "Catálogo não encontrado."
                ))
                .andExpect(jsonPath("$.caminho").value(
                        "/catalogos/" + Long.MAX_VALUE
                ));
    }

    // =========================
    // ATUALIZAR
    // =========================

    @Test
    void deveAtualizarCatalogoComSucesso() throws Exception {

        Catalogo catalogo =
                criarCatalogo("Lamen e Macarrões");

        mockMvc.perform(put("/catalogos/{id}", catalogo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJson(" Lamen e Massas ")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(catalogo.getId()))
                .andExpect(jsonPath("$.nome").value(
                        "Lamen e Massas"
                ));

        catalogoRepository.flush();

        assertTrue(
                catalogoRepository.existsByNomeIgnoreCase(
                        "Lamen e Massas"
                )
        );

        assertFalse(
                catalogoRepository.existsByNomeIgnoreCase(
                        "Lamen e Macarrões"
                )
        );
    }

    @Test
    void deveAtualizarCatalogoMantendoProprioNome()
            throws Exception {

        Catalogo catalogo =
                criarCatalogo("Lamen e Macarrões");

        mockMvc.perform(put("/catalogos/{id}", catalogo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJson("Lamen e Macarrões")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(catalogo.getId()))
                .andExpect(jsonPath("$.nome").value(
                        "Lamen e Macarrões"
                ));

        assertEquals(1, catalogoRepository.count());
    }

    @Test
    void deveRetornar409AoAtualizarComNomeDeOutroCatalogo()
            throws Exception {

        Catalogo catalogo =
                criarCatalogo("Lamen e Macarrões");

        criarCatalogo("Snacks e Doces");

        mockMvc.perform(put("/catalogos/{id}", catalogo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJson("SNACKS E DOCES")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensagem").value(
                        "Já existe um catálogo cadastrado com este nome."
                ));

        assertTrue(
                catalogoRepository.existsByNomeIgnoreCase(
                        "Lamen e Macarrões"
                )
        );

        assertEquals(2, catalogoRepository.count());
    }

    @Test
    void deveRetornar404AoAtualizarCatalogoInexistente()
            throws Exception {

        mockMvc.perform(put("/catalogos/{id}", Long.MAX_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJson("Snacks e Doces")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value(
                        "Catálogo não encontrado."
                ));
    }

    @Test
    void deveRetornar400AoAtualizarComNomeInvalido()
            throws Exception {

        Catalogo catalogo =
                criarCatalogo("Lamen e Macarrões");

        mockMvc.perform(put("/catalogos/{id}", catalogo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJson("")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").value(
                        "O nome do catálogo é obrigatório."
                ));

        assertTrue(
                catalogoRepository.existsByNomeIgnoreCase(
                        "Lamen e Macarrões"
                )
        );
    }

    // =========================
    // EXCLUIR
    // =========================

    @Test
    void deveExcluirCatalogoComSucesso() throws Exception {

        Catalogo catalogo =
                criarCatalogo("Lamen e Macarrões");

        mockMvc.perform(delete("/catalogos/{id}", catalogo.getId()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        catalogoRepository.flush();

        assertFalse(
                catalogoRepository.existsById(catalogo.getId())
        );
    }

    @Test
    void deveRetornar404AoExcluirCatalogoInexistente()
            throws Exception {

        mockMvc.perform(delete("/catalogos/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value(
                        "Catálogo não encontrado."
                ));
    }

    @Test
    void deveRetornar409AoExcluirCatalogoComProdutosAssociados()
            throws Exception {

        Catalogo catalogo =
                criarCatalogo("Lamen e Macarrões");

        Produto produto = criarProdutoAssociado(catalogo);

        mockMvc.perform(delete("/catalogos/{id}", catalogo.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensagem").value(
                        "Não é possível excluir o catálogo, pois existem produtos associados!"
                ));

        assertTrue(
                catalogoRepository.existsById(catalogo.getId())
        );

        assertTrue(
                produtoRepository.existsById(produto.getId())
        );

        assertTrue(
                produtoRepository.existsByCatalogoId(
                        catalogo.getId()
                )
        );
    }
}
