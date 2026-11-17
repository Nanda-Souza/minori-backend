package com.db.minori_backend.domain.specification;

import com.db.minori_backend.domain.entity.Produto;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ProdutoSpecification {

    public static Specification<Produto> comFiltros(
            Long catalogoId,
            Long marcaId,
            Long categoriaId,
            BigDecimal precoMin,
            BigDecimal precoMax
    ) {
        return (root, query, cb) -> {

            var predicates = new java.util.ArrayList<
                    jakarta.persistence.criteria.Predicate>();

            if (catalogoId != null) {
                predicates.add(
                        cb.equal(root.get("catalogo").get("id"), catalogoId)
                );
            }

            if (marcaId != null) {
                predicates.add(
                        cb.equal(root.get("marca").get("id"), marcaId)
                );
            }

            if (categoriaId != null) {
                predicates.add(
                        cb.equal(root.get("categoria").get("id"), categoriaId)
                );
            }

            if (precoMin != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(root.get("preco"), precoMin)
                );
            }

            if (precoMax != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(root.get("preco"), precoMax)
                );
            }

            return cb.and(predicates.toArray(
                    new jakarta.persistence.criteria.Predicate[0]
            ));
        };
    }
}