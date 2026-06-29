package com.shopconnect.ms_inventario.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopconnect.ms_inventario.model.Inventario;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    // Encontrar el producto. Usamos Optional para validar si hay un dato no nulo dentro del objeto. 
    Optional<Inventario> findByProductoId(Long productoId);

    // Revisamos si existe por productoId. Esto es para evitar duplicados. Entrega un verdadero o falso al ser boolean
    boolean existsByProductoId(Long productoId);

    

}