package com.db.minori_backend.domain.repository;

import com.db.minori_backend.domain.entity.Marca;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarcaRepository extends JpaRepository<Marca, Long> {
}