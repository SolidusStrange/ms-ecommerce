package com.shopconnect.ms_productos.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.shopconnect.ms_productos.model.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}