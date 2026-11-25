
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
class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private CatalogoRepository catalogoRepository;

    @Autowired
    private MarcaRepository marcaRepository;

    private Categoria criarCategoria(String nome) {

        Categoria categoria = new Categoria();
        categoria.setNome(nome);

        return categoriaRepository.saveAndFlush(categoria);
    }

    private String criarJson(String nome) {

        return """
                {
                    "nome": "%s"
                }
                """.formatted(nome);
    }

    private Produto criarProdutoAssociado(Categoria categoria) {

        Catalogo catalogo = new Catalogo();
        catalogo.setNome("Lamen e Macarrões");
        catalogo = catalogoRepository.save(catalogo);

        Marca marca = new Marca();
        marca.setNome("Nissin");
        marca = marcaRepository.save(marca);

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
    void deveCadastrarCategoriaComSucesso() throws Exception {

        mockMvc.perform(post("/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJson("Macarrão instantâneo")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value(
                        "Macarrão instantâneo"
                ));

        assertTrue(
                categoriaRepository.existsByNomeIgnoreCase(
                        "Macarrão instantâneo"
                )
        );
    }

    @Test
    void deveCadastrarCategoriaRemovendoEspacos() throws Exception {

        mockMvc.perform(post("/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJson(" Macarrão instantâneo ")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value(
                        "Macarrão instantâneo"
                ));

        assertTrue(
                categoriaRepository.existsByNomeIgnoreCase(
                        "Macarrão instantâneo"
                )
        );

        assertFalse(
                categoriaRepository.existsByNomeIgnoreCase(
                        " Macarrão instantâneo "
                )
        );
    }

    @Test
    void deveRetornar409AoCadastrarCategoriaDuplicada()
            throws Exception {

        criarCategoria("Macarrão instantâneo");

        mockMvc.perform(post("/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJson("Macarrão instantâneo")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.erro").value("Conflict"))
                .andExpect(jsonPath("$.mensagem").value(
                        "Já existe uma categoria cadastrada com este nome."
                ))
                .andExpect(jsonPath("$.caminho").value("/categorias"))
                .andExpect(jsonPath("$.timestamp").exists());

        assertEquals(1, categoriaRepository.count());
    }

    @Test
    void deveRetornar409AoCadastrarNomeDuplicadoEmMaiusculas()
            throws Exception {

        criarCategoria("Macarrão instantâneo");

        mockMvc.perform(post("/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJson("MACARRÃO INSTANTÂNEO")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensagem").value(
                        "Já existe uma categoria cadastrada com este nome."
                ));

        assertEquals(1, categoriaRepository.count());
    }

    @Test
    void deveRetornar409AoCadastrarNomeDuplicadoComEspacos()
            throws Exception {

        criarCategoria("Macarrão instantâneo");

        mockMvc.perform(post("/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJson(" Macarrão instantâneo ")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensagem").value(
                        "Já existe uma categoria cadastrada com este nome."
                ));

        assertEquals(1, categoriaRepository.count());
    }

    @Test
    void deveRetornar400AoCadastrarCategoriaSemNome()
            throws Exception {

        mockMvc.perform(post("/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJson("")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value(
                        "Existem campos inválidos na requisição."
                ))
                .andExpect(jsonPath("$.campos.nome").value(
                        "O nome da categoria é obrigatório."
                ));

        assertEquals(0, categoriaRepository.count());
    }

    @Test
    void deveRetornar400QuandoNomePossuirApenasEspacos()
            throws Exception {

        mockMvc.perform(post("/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJson("   ")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").value(
                        "O nome da categoria é obrigatório."
                ));

        assertEquals(0, categoriaRepository.count());
    }

    @Test
    void deveRetornar400QuandoNomeForNulo()
            throws Exception {

        String json = """
                {
                    "nome": null
                }
                """;

        mockMvc.perform(post("/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").value(
                        "O nome da categoria é obrigatório."
                ));

        assertEquals(0, categoriaRepository.count());
    }

    @Test
    void deveCadastrarCategoriaCom200Caracteres()
            throws Exception {

        String nome = "A".repeat(200);

        mockMvc.perform(post("/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJson(nome)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value(nome));

        assertTrue(
                categoriaRepository.existsByNomeIgnoreCase(nome)
        );
    }

    @Test
    void deveRetornar400QuandoNomeUltrapassar200Caracteres()
            throws Exception {

        String nome = "A".repeat(201);

        mockMvc.perform(post("/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJson(nome)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").value(
                        "O nome deve ter no máximo 200 caracteres."
                ));

        assertEquals(0, categoriaRepository.count());
    }

    @Test
    void deveRetornar400QuandoJsonForInvalido()
            throws Exception {

        mockMvc.perform(post("/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(
                        "O corpo da requisição está inválido ou malformado."
                ));

        assertEquals(0, categoriaRepository.count());
    }

    // =========================
    // LISTAR TODOS
    // =========================

    @Test
    void deveListarTodasAsCategorias() throws Exception {

        criarCategoria("Macarrão instantâneo");
        criarCategoria("Biscoitos");

        mockMvc.perform(get("/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].nome")
                        .value(containsInAnyOrder(
                                "Macarrão instantâneo",
                                "Biscoitos"
                        )));
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremCategorias()
            throws Exception {

        mockMvc.perform(get("/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // =========================
    // BUSCAR POR ID
    // =========================

    @Test
    void deveBuscarCategoriaPorId() throws Exception {

        Categoria categoria =
                criarCategoria("Macarrão instantâneo");

        mockMvc.perform(get("/categorias/{id}", categoria.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoria.getId()))
                .andExpect(jsonPath("$.nome").value(
                        "Macarrão instantâneo"
                ));
    }

    @Test
    void deveRetornar404AoBuscarCategoriaInexistente()
            throws Exception {

        mockMvc.perform(get("/categorias/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.erro").value("Not Found"))
                .andExpect(jsonPath("$.mensagem").value(
                        "Categoria não encontrada."
                ))
                .andExpect(jsonPath("$.caminho").value(
                        "/categorias/" + Long.MAX_VALUE
                ));
    }

    // =========================
    // ATUALIZAR
    // =========================

    @Test
    void deveAtualizarCategoriaComSucesso() throws Exception {

        Categoria categoria =
                criarCategoria("Macarrão instantâneo");

        mockMvc.perform(put("/categorias/{id}", categoria.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJson(" Macarrões asiáticos ")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoria.getId()))
                .andExpect(jsonPath("$.nome").value(
                        "Macarrões asiáticos"
                ));

        categoriaRepository.flush();

        assertTrue(
                categoriaRepository.existsByNomeIgnoreCase(
                        "Macarrões asiáticos"
                )
        );

        assertFalse(
                categoriaRepository.existsByNomeIgnoreCase(
                        "Macarrão instantâneo"
                )
        );
    }

    @Test
    void deveAtualizarCategoriaMantendoProprioNome()
            throws Exception {

        Categoria categoria =
                criarCategoria("Macarrão instantâneo");

        mockMvc.perform(put("/categorias/{id}", categoria.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJson("Macarrão instantâneo")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoria.getId()))
                .andExpect(jsonPath("$.nome").value(
                        "Macarrão instantâneo"
                ));

        assertEquals(1, categoriaRepository.count());
    }

    @Test
    void deveRetornar409AoAtualizarComNomeDeOutraCategoria()
            throws Exception {

        Categoria categoria =
                criarCategoria("Macarrão instantâneo");

        criarCategoria("Biscoitos");

        mockMvc.perform(put("/categorias/{id}", categoria.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJson("BISCOITOS")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensagem").value(
                        "Já existe uma categoria cadastrada com este nome."
                ));

        assertTrue(
                categoriaRepository.existsByNomeIgnoreCase(
                        "Macarrão instantâneo"
                )
        );

        assertEquals(2, categoriaRepository.count());
    }

    @Test
    void deveRetornar404AoAtualizarCategoriaInexistente()
            throws Exception {

        mockMvc.perform(put("/categorias/{id}", Long.MAX_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJson("Biscoitos")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value(
                        "Categoria não encontrada."
                ));
    }

    @Test
    void deveRetornar400AoAtualizarComNomeInvalido()
            throws Exception {

        Categoria categoria =
                criarCategoria("Macarrão instantâneo");

        mockMvc.perform(put("/categorias/{id}", categoria.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJson("")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").value(
                        "O nome da categoria é obrigatório."
                ));

        assertTrue(
                categoriaRepository.existsByNomeIgnoreCase(
                        "Macarrão instantâneo"
                )
        );
    }

    // =========================
    // EXCLUIR
    // =========================

    @Test
    void deveExcluirCategoriaComSucesso() throws Exception {

        Categoria categoria =
                criarCategoria("Macarrão instantâneo");

        mockMvc.perform(delete("/categorias/{id}", categoria.getId()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        categoriaRepository.flush();

        assertFalse(
                categoriaRepository.existsById(categoria.getId())
        );
    }

    @Test
    void deveRetornar404AoExcluirCategoriaInexistente()
            throws Exception {

        mockMvc.perform(delete("/categorias/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value(
                        "Categoria não encontrada."
                ));
    }

    @Test
    void deveRetornar409AoExcluirCategoriaComProdutosAssociados()
            throws Exception {

        Categoria categoria =
                criarCategoria("Macarrão instantâneo");

        Produto produto = criarProdutoAssociado(categoria);

        mockMvc.perform(delete("/categorias/{id}", categoria.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensagem").value(
                        "Não é possível excluir a categoria, pois existem produtos associados!"
                ));

        assertTrue(
                categoriaRepository.existsById(categoria.getId())
        );

        assertTrue(
                produtoRepository.existsById(produto.getId())
        );

        assertTrue(
                produtoRepository.existsByCategoriaId(
                        categoria.getId()
                )
        );
    }
}
