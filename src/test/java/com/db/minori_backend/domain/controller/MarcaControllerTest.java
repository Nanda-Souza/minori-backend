
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MarcaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MarcaRepository marcaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private CatalogoRepository catalogoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private Marca criarMarca(String nome) {

        Marca marca = new Marca();
        marca.setNome(nome);

        return marcaRepository.saveAndFlush(marca);
    }

    private String criarJson(String nome) {

        return """
                {
                    "nome": "%s"
                }
                """.formatted(nome);
    }

    private Produto criarProdutoAssociado(Marca marca) {

        Catalogo catalogo = new Catalogo();
        catalogo.setNome("Lamen e Macarrões");
        catalogo = catalogoRepository.save(catalogo);

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
    void deveCadastrarMarcaComSucesso() throws Exception {

        mockMvc.perform(post("/marcas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJson("Nissin")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Nissin"));

        assertTrue(
                marcaRepository.existsByNomeIgnoreCase("Nissin")
        );
    }

    @Test
    void deveCadastrarMarcaRemovendoEspacos() throws Exception {

        mockMvc.perform(post("/marcas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJson(" Nissin ")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Nissin"));

        assertTrue(
                marcaRepository.existsByNomeIgnoreCase("Nissin")
        );

        assertFalse(
                marcaRepository.existsByNomeIgnoreCase(" Nissin ")
        );
    }

    @Test
    void deveRetornar409AoCadastrarMarcaDuplicada()
            throws Exception {

        criarMarca("Nissin");

        mockMvc.perform(post("/marcas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJson("Nissin")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.erro").value("Conflict"))
                .andExpect(jsonPath("$.mensagem").value(
                        "Já existe uma marca cadastrada com este nome."
                ))
                .andExpect(jsonPath("$.caminho").value("/marcas"))
                .andExpect(jsonPath("$.timestamp").exists());

        assertEquals(1, marcaRepository.count());
    }

    @Test
    void deveRetornar409AoCadastrarNomeDuplicadoEmMaiusculas()
            throws Exception {

        criarMarca("Nissin");

        mockMvc.perform(post("/marcas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJson("NISSIN")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensagem").value(
                        "Já existe uma marca cadastrada com este nome."
                ));

        assertEquals(1, marcaRepository.count());
    }

    @Test
    void deveRetornar409AoCadastrarNomeDuplicadoComEspacos()
            throws Exception {

        criarMarca("Nissin");

        mockMvc.perform(post("/marcas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJson(" Nissin ")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensagem").value(
                        "Já existe uma marca cadastrada com este nome."
                ));

        assertEquals(1, marcaRepository.count());
    }

    @Test
    void deveRetornar400AoCadastrarMarcaSemNome()
            throws Exception {

        mockMvc.perform(post("/marcas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJson("")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value(
                        "Existem campos inválidos na requisição."
                ))
                .andExpect(jsonPath("$.campos.nome").value(
                        "O nome da marca é obrigatório."
                ));

        assertEquals(0, marcaRepository.count());
    }

    @Test
    void deveRetornar400QuandoNomePossuirApenasEspacos()
            throws Exception {

        mockMvc.perform(post("/marcas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJson("   ")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").value(
                        "O nome da marca é obrigatório."
                ));

        assertEquals(0, marcaRepository.count());
    }

    @Test
    void deveRetornar400QuandoNomeForNulo()
            throws Exception {

        String json = """
                {
                    "nome": null
                }
                """;

        mockMvc.perform(post("/marcas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").value(
                        "O nome da marca é obrigatório."
                ));

        assertEquals(0, marcaRepository.count());
    }

    @Test
    void deveCadastrarMarcaCom200Caracteres()
            throws Exception {

        String nome = "A".repeat(200);

        mockMvc.perform(post("/marcas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJson(nome)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value(nome));

        assertTrue(
                marcaRepository.existsByNomeIgnoreCase(nome)
        );
    }

    @Test
    void deveRetornar400QuandoNomeUltrapassar200Caracteres()
            throws Exception {

        String nome = "A".repeat(201);

        mockMvc.perform(post("/marcas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJson(nome)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").value(
                        "O nome deve ter no máximo 200 caracteres."
                ));

        assertEquals(0, marcaRepository.count());
    }

    @Test
    void deveRetornar400QuandoJsonForInvalido()
            throws Exception {

        mockMvc.perform(post("/marcas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(
                        "O corpo da requisição está inválido ou malformado."
                ));

        assertEquals(0, marcaRepository.count());
    }

    // =========================
    // LISTAR TODOS
    // =========================

    @Test
    void deveListarTodasAsMarcas() throws Exception {

        criarMarca("Nissin");
        criarMarca("Samyang");

        mockMvc.perform(get("/marcas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].nome")
                        .value(org.hamcrest.Matchers.containsInAnyOrder(
                                "Nissin",
                                "Samyang"
                        )));
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremMarcas()
            throws Exception {

        mockMvc.perform(get("/marcas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // =========================
    // BUSCAR POR ID
    // =========================

    @Test
    void deveBuscarMarcaPorId() throws Exception {

        Marca marca = criarMarca("Nissin");

        mockMvc.perform(get("/marcas/{id}", marca.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(marca.getId()))
                .andExpect(jsonPath("$.nome").value("Nissin"));
    }

    @Test
    void deveRetornar404AoBuscarMarcaInexistente()
            throws Exception {

        mockMvc.perform(get("/marcas/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.erro").value("Not Found"))
                .andExpect(jsonPath("$.mensagem").value(
                        "Marca não encontrada."
                ))
                .andExpect(jsonPath("$.caminho").value(
                        "/marcas/" + Long.MAX_VALUE
                ));
    }

    // =========================
    // ATUALIZAR
    // =========================

    @Test
    void deveAtualizarMarcaComSucesso() throws Exception {

        Marca marca = criarMarca("Nissin");

        mockMvc.perform(put("/marcas/{id}", marca.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJson(" Nissin Foods ")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(marca.getId()))
                .andExpect(jsonPath("$.nome").value("Nissin Foods"));

        marcaRepository.flush();

        assertTrue(
                marcaRepository.existsByNomeIgnoreCase("Nissin Foods")
        );

        assertFalse(
                marcaRepository.existsByNomeIgnoreCase("Nissin")
        );
    }

    @Test
    void deveAtualizarMarcaMantendoProprioNome()
            throws Exception {

        Marca marca = criarMarca("Nissin");

        mockMvc.perform(put("/marcas/{id}", marca.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJson("Nissin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(marca.getId()))
                .andExpect(jsonPath("$.nome").value("Nissin"));

        assertEquals(1, marcaRepository.count());
    }

    @Test
    void deveRetornar409AoAtualizarComNomeDeOutraMarca()
            throws Exception {

        Marca marca = criarMarca("Nissin");

        criarMarca("Samyang");

        mockMvc.perform(put("/marcas/{id}", marca.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJson("SAMYANG")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensagem").value(
                        "Já existe uma marca cadastrada com este nome."
                ));

        assertTrue(
                marcaRepository.existsByNomeIgnoreCase("Nissin")
        );

        assertEquals(2, marcaRepository.count());
    }

    @Test
    void deveRetornar404AoAtualizarMarcaInexistente()
            throws Exception {

        mockMvc.perform(put("/marcas/{id}", Long.MAX_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJson("Nissin")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value(
                        "Marca não encontrada."
                ));
    }

    @Test
    void deveRetornar400AoAtualizarComNomeInvalido()
            throws Exception {

        Marca marca = criarMarca("Nissin");

        mockMvc.perform(put("/marcas/{id}", marca.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJson("")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").value(
                        "O nome da marca é obrigatório."
                ));

        assertTrue(
                marcaRepository.existsByNomeIgnoreCase("Nissin")
        );
    }

    // =========================
    // EXCLUIR
    // =========================

    @Test
    void deveExcluirMarcaComSucesso() throws Exception {

        Marca marca = criarMarca("Nissin");

        mockMvc.perform(delete("/marcas/{id}", marca.getId()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        marcaRepository.flush();

        assertFalse(
                marcaRepository.existsById(marca.getId())
        );
    }

    @Test
    void deveRetornar404AoExcluirMarcaInexistente()
            throws Exception {

        mockMvc.perform(delete("/marcas/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value(
                        "Marca não encontrada."
                ));
    }

    @Test
    void deveRetornar409AoExcluirMarcaComProdutosAssociados()
            throws Exception {

        Marca marca = criarMarca("Nissin");

        Produto produto = criarProdutoAssociado(marca);

        mockMvc.perform(delete("/marcas/{id}", marca.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensagem").value(
                        "Não é possível excluir a marca, pois existem produtos associados!"
                ));

        assertTrue(
                marcaRepository.existsById(marca.getId())
        );

        assertTrue(
                produtoRepository.existsById(produto.getId())
        );

        assertTrue(
                produtoRepository.existsByMarcaId(marca.getId())
        );
    }
}
