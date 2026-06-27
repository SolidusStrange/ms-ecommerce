package com.shopconnect.ms_pagos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopconnect.ms_pagos.model.MetodoPago;

public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Long> {

    boolean existsByNombre(String nombre);

    List<MetodoPago> findByActivo(Boolean activo);
}