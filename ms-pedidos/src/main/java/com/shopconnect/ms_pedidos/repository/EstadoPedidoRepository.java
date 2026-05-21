package com.shopconnect.ms_pedidos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopconnect.ms_pedidos.model.EstadoPedido;

public interface EstadoPedidoRepository extends JpaRepository<EstadoPedido, Long> {

    boolean existsByNombre(String nombre);
}