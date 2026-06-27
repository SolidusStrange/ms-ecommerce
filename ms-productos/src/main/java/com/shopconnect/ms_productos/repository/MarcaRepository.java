package com.shopconnect.ms_productos.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.shopconnect.ms_productos.model.Marca;

public interface MarcaRepository extends JpaRepository<Marca, Long> {

    
}

