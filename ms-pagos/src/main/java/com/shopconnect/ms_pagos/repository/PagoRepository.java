package com.shopconnect.ms_pagos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopconnect.ms_pagos.model.Pago;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    List<Pago> findByPedidoId(Long pedidoId);

    List<Pago> findByEstado(String estado);
}