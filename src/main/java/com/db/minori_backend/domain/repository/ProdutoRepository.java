package com.db.minori_backend.domain.repository;

import com.db.minori_backend.domain.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    boolean existsByMarcaId(Long marcaId);

    boolean existsByCategoriaId(Long categoriaId);

    boolean existsByCatalogoId(Long catalogoId);
}