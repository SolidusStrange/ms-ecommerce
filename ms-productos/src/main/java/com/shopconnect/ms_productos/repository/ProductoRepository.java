package com.shopconnect.ms_productos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopconnect.ms_productos.model.Producto;


public interface ProductoRepository extends JpaRepository<Producto, Long> {
    
    List<Producto> findByCategoriaId(Long categoriaId);

    List<Producto> findByMarcaId(Long marcaId);

    boolean existsBySku(String sku);

    List<Producto> findByNombreContainingIgnoreCase(String nombre);


}
