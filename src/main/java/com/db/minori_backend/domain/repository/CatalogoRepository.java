package com.db.minori_backend.domain.repository;

import com.db.minori_backend.domain.entity.Catalogo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatalogoRepository extends JpaRepository<Catalogo, Long> {
}