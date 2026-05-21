package com.shopconnect.ms_pagos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopconnect.ms_pagos.model.TransaccionPago;

public interface TransaccionPagoRepository extends JpaRepository<TransaccionPago, Long> {

    List<TransaccionPago> findByPagoId(Long pagoId);
}