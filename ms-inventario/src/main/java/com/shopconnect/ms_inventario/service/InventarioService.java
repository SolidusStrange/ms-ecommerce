package com.shopconnect.ms_inventario.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopconnect.ms_inventario.model.Inventario;
import com.shopconnect.ms_inventario.model.MovimientoInventario;
import com.shopconnect.ms_inventario.repository.InventarioRepository;
import com.shopconnect.ms_inventario.repository.MovimientoInventarioRepository;

/**
 * SERVICIO: InventarioService
 *
 * RESPONSABILIDAD: Lógica de negocio.
 * El Service llama a los métodos del Repository.
 * El Service NO escribe SQL ni usa EntityManager directamente.
 */
@Service
public class InventarioService {

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private MovimientoInventarioRepository movimientoInventarioRepository;

    // ═══ INVENTARIO ════════════════════════════════════════════════════

    public List<Inventario> listarTodos() {
        return inventarioRepository.findAll();
    }

    public Optional<Inventario> buscarPorId(Long id) {
        return inventarioRepository.findById(id);
    }

    public Optional<Inventario> buscarPorProductoId(Long productoId) {
        return inventarioRepository.findByProductoId(productoId);
    }

    @Transactional
    public Inventario crear(Inventario inventario) {

        if (inventarioRepository.existsByProductoId(inventario.getProductoId())) {
            throw new IllegalArgumentException("Ya existe inventario para el productoId: " + inventario.getProductoId());
        }

        if (inventario.getStockActual() == null) {
            inventario.setStockActual(0);
        }

        if (inventario.getStockMinimo() == null) {
            inventario.setStockMinimo(0);
        }

        return inventarioRepository.save(inventario);
    }

    @Transactional
    public Inventario actualizar(Long id, Inventario datos) {

        Inventario inventario = inventarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado: " + id));

        if (datos.getProductoId() != null) {
            if (!datos.getProductoId().equals(inventario.getProductoId())
                    && inventarioRepository.existsByProductoId(datos.getProductoId())) {
                throw new IllegalArgumentException("Ya existe inventario para el productoId: " + datos.getProductoId());
            }

            inventario.setProductoId(datos.getProductoId());
        }

        if (datos.getStockActual() != null) {
            inventario.setStockActual(datos.getStockActual());
        }

        if (datos.getStockMinimo() != null) {
            inventario.setStockMinimo(datos.getStockMinimo());
        }

        return inventarioRepository.save(inventario);
    }

    @Transactional
    public void eliminar(Long id) {

        if (!inventarioRepository.existsById(id)) {
            throw new RuntimeException("Inventario no encontrado: " + id);
        }

        inventarioRepository.deleteById(id);
    }

    // ═══ MOVIMIENTOS DE INVENTARIO ═════════════════════════════════════

    public List<MovimientoInventario> listarMovimientosPorInventario(Long inventarioId) {
        return movimientoInventarioRepository.findByInventarioId(inventarioId);
    }

    @Transactional
    public MovimientoInventario registrarMovimiento(Long inventarioId, MovimientoInventario movimiento) {

        Inventario inventario = inventarioRepository.findById(inventarioId)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado: " + inventarioId));

        if (movimiento.getCantidad() == null || movimiento.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }

        if (movimiento.getTipo() == null || movimiento.getTipo().isBlank()) {
            throw new IllegalArgumentException("El tipo de movimiento es obligatorio");
        }

        String tipo = movimiento.getTipo().toUpperCase();

        switch (tipo) {
            case "ENTRADA" -> inventario.setStockActual(inventario.getStockActual() + movimiento.getCantidad());
            case "SALIDA" -> {
                if (inventario.getStockActual() < movimiento.getCantidad()) {
                    throw new IllegalStateException("Stock insuficiente para realizar la salida");
                }   inventario.setStockActual(inventario.getStockActual() - movimiento.getCantidad());
            }
            default -> throw new IllegalArgumentException("Tipo de movimiento inválido. Use ENTRADA o SALIDA");
        }

        movimiento.setTipo(tipo);
        movimiento.setInventario(inventario);

        if (movimiento.getFechaMovimiento() == null) {
            movimiento.setFechaMovimiento(LocalDateTime.now());
        }

        inventarioRepository.save(inventario);

        return movimientoInventarioRepository.save(movimiento);
    }

    // ═══ OPERACIONES ESPECÍFICAS ═══════════════════════════════════════

    @Transactional
    public Inventario ajustarStockMinimo(Long inventarioId, Integer stockMinimo) {

        Inventario inventario = inventarioRepository.findById(inventarioId)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado: " + inventarioId));

        if (stockMinimo == null || stockMinimo < 0) {
            throw new IllegalArgumentException("El stock mínimo no puede ser negativo");
        }

        inventario.setStockMinimo(stockMinimo);

        return inventarioRepository.save(inventario);
    }
}