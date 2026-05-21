package com.shopconnect.ms_pedidos.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopconnect.ms_pedidos.model.DetallePedido;
import com.shopconnect.ms_pedidos.model.EstadoPedido;
import com.shopconnect.ms_pedidos.model.Pedido;
import com.shopconnect.ms_pedidos.repository.DetallePedidoRepository;
import com.shopconnect.ms_pedidos.repository.EstadoPedidoRepository;
import com.shopconnect.ms_pedidos.repository.PedidoRepository;

/**
 * SERVICIO: PedidoService
 *
 * RESPONSABILIDAD: Lógica de negocio.
 * El Service llama a los métodos del Repository.
 * El Service NO escribe SQL ni usa EntityManager directamente.
 */
@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private EstadoPedidoRepository estadoPedidoRepository;

    @Autowired
    private DetallePedidoRepository detallePedidoRepository;

    // ═══ PEDIDOS ════════════════════════════════════════════════════

    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }

    public Optional<Pedido> buscarPorId(Long id) {
        return pedidoRepository.findById(id);
    }

    public List<Pedido> listarPorUsuario(Long usuarioId) {
        return pedidoRepository.findByUsuarioId(usuarioId);
    }

    public List<Pedido> listarPorEstado(Long estadoId) {
        return pedidoRepository.findByEstadoId(estadoId);
    }

    @Transactional
    public Pedido crear(Pedido pedido, Long estadoId) {

        // Validar que existe el estado inicial del pedido
        EstadoPedido estado = estadoPedidoRepository.findById(estadoId)
                .orElseThrow(() -> new RuntimeException("Estado no encontrado: " + estadoId));

        pedido.setEstado(estado);

        if (pedido.getFechaPedido() == null) {
            pedido.setFechaPedido(LocalDateTime.now());
        }

        // Asignar el pedido a cada detalle y calcular total
        double total = 0.0;

        if (pedido.getDetalles() != null && !pedido.getDetalles().isEmpty()) {
            for (DetallePedido detalle : pedido.getDetalles()) {
                detalle.setPedido(pedido);

                double subtotal = detalle.getCantidad() * detalle.getPrecioUnit();
                total += subtotal;
            }
        }

        pedido.setTotal(total);

        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido actualizar(Long id, Pedido datos) {

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + id));

        if (datos.getUsuarioId() != null) {
            pedido.setUsuarioId(datos.getUsuarioId());
        }

        if (datos.getFechaPedido() != null) {
            pedido.setFechaPedido(datos.getFechaPedido());
        }

        return pedidoRepository.save(pedido);
    }

    @Transactional
    public void eliminar(Long id) {

        if (!pedidoRepository.existsById(id)) {
            throw new RuntimeException("Pedido no encontrado: " + id);
        }

        pedidoRepository.deleteById(id);
    }

    // ═══ ESTADOS DE PEDIDO ════════════════════════════════════════════

    public List<EstadoPedido> listarEstados() {
        return estadoPedidoRepository.findAll();
    }

    public Optional<EstadoPedido> buscarEstadoPorId(Long id) {
        return estadoPedidoRepository.findById(id);
    }

    @Transactional
    public EstadoPedido crearEstado(EstadoPedido estado) {

        if (estadoPedidoRepository.existsByNombre(estado.getNombre())) {
            throw new IllegalArgumentException("Nombre de estado duplicado: " + estado.getNombre());
        }

        return estadoPedidoRepository.save(estado);
    }

    @Transactional
    public void eliminarEstado(Long id) {

        if (!estadoPedidoRepository.existsById(id)) {
            throw new RuntimeException("Estado no encontrado: " + id);
        }

        List<Pedido> pedidosConEstado = pedidoRepository.findByEstadoId(id);

        if (!pedidosConEstado.isEmpty()) {
            throw new IllegalStateException("El estado tiene pedidos asociados");
        }

        estadoPedidoRepository.deleteById(id);
    }

    // ═══ DETALLES DE PEDIDO ═══════════════════════════════════════════

    public List<DetallePedido> listarDetallesPorPedido(Long pedidoId) {
        return detallePedidoRepository.findByPedidoId(pedidoId);
    }

    public List<DetallePedido> listarDetallesPorProducto(Long productoId) {
        return detallePedidoRepository.findByProductoId(productoId);
    }

    @Transactional
    public DetallePedido agregarDetalle(Long pedidoId, DetallePedido detalle) {

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + pedidoId));

        detalle.setPedido(pedido);

        DetallePedido detalleGuardado = detallePedidoRepository.save(detalle);

        recalcularTotal(pedido);

        return detalleGuardado;
    }

    @Transactional
    public void eliminarDetalle(Long id) {

        DetallePedido detalle = detallePedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Detalle no encontrado: " + id));

        Pedido pedido = detalle.getPedido();

        detallePedidoRepository.deleteById(id);

        recalcularTotal(pedido);
    }

    // ═══ OPERACIONES ESPECÍFICAS ═══════════════════════════════════════

    @Transactional
    public Pedido cambiarEstado(Long pedidoId, Long estadoId) {

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + pedidoId));

        EstadoPedido estado = estadoPedidoRepository.findById(estadoId)
                .orElseThrow(() -> new RuntimeException("Estado no encontrado: " + estadoId));

        pedido.setEstado(estado);

        return pedidoRepository.save(pedido);
    }

    // ═══ MÉTODOS PRIVADOS ═════════════════════════════════════════════

    private void recalcularTotal(Pedido pedido) {

        List<DetallePedido> detalles = detallePedidoRepository.findByPedidoId(pedido.getId());

        double total = 0.0;

        for (DetallePedido detalle : detalles) {
            total += detalle.getCantidad() * detalle.getPrecioUnit();
        }

        pedido.setTotal(total);

        pedidoRepository.save(pedido);
    }
}