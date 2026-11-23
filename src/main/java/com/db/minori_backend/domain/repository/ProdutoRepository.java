package com.db.minori_backend.domain.repository;

import com.db.minori_backend.domain.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface ProdutoRepository extends
        JpaRepository<Produto, Long>,
        JpaSpecificationExecutor<Produto>{

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    boolean existsByMarcaId(Long marcaId);

    boolean existsByCategoriaId(Long categoriaId);

    boolean existsByCatalogoId(Long catalogoId);
}