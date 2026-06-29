package com.shopconnect.ms_inventario.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopconnect.ms_inventario.model.MovimientoInventario;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    // Busca los movimientos asociados a ese Id de inventario en la base de datos. Es formato lista porque pueden haber varios movimientos.
    List<MovimientoInventario> findByInventarioId(Long inventarioId);
}