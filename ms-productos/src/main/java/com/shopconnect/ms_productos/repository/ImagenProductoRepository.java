package com.shopconnect.ms_productos.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopconnect.ms_productos.model.ImagenProducto;

public interface ImagenProductoRepository extends JpaRepository<ImagenProducto, Long> {

    List<ImagenProducto> findByProductoId(Long productoId);
}