package com.shopconnect.ms_pedidos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopconnect.ms_pedidos.model.DetallePedido;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    List<DetallePedido> findByPedidoId(Long pedidoId);

    List<DetallePedido> findByProductoId(Long productoId);
}