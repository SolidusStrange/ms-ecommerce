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
import com.shopconnect.ms_inventario.dto.request.InventarioRequestDTO;
import com.shopconnect.ms_inventario.dto.response.InventarioResponseDTO;
import com.shopconnect.ms_inventario.dto.request.MovimientoInventarioRequestDTO;
import com.shopconnect.ms_inventario.dto.response.MovimientoInventarioResponseDTO;

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
    public InventarioResponseDTO crear(InventarioRequestDTO dto) { // Recibes el request DTO

        if (inventarioRepository.existsByProductoId(dto.getProductoId())) { // Validar duplicado
            throw new IllegalArgumentException("Ya existe inventario para el productoId: " + dto.getProductoId());
        }

        Inventario inventario = new Inventario(); // Crear objeto si no esta duplicado

        // Revisar stock actual y si es null es igual a 0. De lo contrario lo que recibe del DTO
        if (dto.getStockActual() == null) {
            inventario.setStockActual(0);
        } else {
            inventario.setStockActual(dto.getStockActual());
        }

        // Revisar stock minimo y si es diferente a null es igual a 0. De lo contrario lo que decibe del DTO
        if (dto.getStockMinimo() == null) {
            inventario.setStockMinimo(0);
        } else {
            inventario.setStockMinimo(dto.getStockMinimo());
        }

        // Guardar 
        Inventario guardado = inventarioRepository.save(inventario);

        // Response DTO
        InventarioResponseDTO response = new InventarioResponseDTO();
        response.setId(guardado.getId());
        response.setProductoId(guardado.getProductoId());
        response.setStockActual(guardado.getStockActual());
        response.setStockMinimo(guardado.getStockMinimo());

        return response;
    }

    @Transactional
    public InventarioResponseDTO actualizar(Long id, InventarioRequestDTO dto) {

        // buscar por ID en la BD
        Inventario inventario = inventarioRepository.findById(id) 
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado: " + id)); 

        // Si productoId viene informado, validamos que no esté asignado a otro inventario
        if (dto.getProductoId() != null) {
            if (!dto.getProductoId().equals(inventario.getProductoId())
                    && inventarioRepository.existsByProductoId(dto.getProductoId())) {
                throw new IllegalArgumentException(
                    "Ya existe inventario para el productoId: " + dto.getProductoId());
            }

            // Si existe lo actualizamos
            inventario.setProductoId(dto.getProductoId());
        }

        // Si no es nulo actualizamos con el nuevo stock
        if (dto.getStockActual() != null) {
            inventario.setStockActual(dto.getStockActual());
        }

        // Si no es nulo actualizamos con el nuevo stock
        if (dto.getStockMinimo() != null) {
            inventario.setStockMinimo(dto.getStockMinimo());
        }

        // Creamos el objeto actualizado y le asignamos el metodo de JPA para guardar  
        Inventario actualizado = inventarioRepository.save(inventario);

        // Creamos la respuesta, instanciando response y preparandolo para recibir los setters
        InventarioResponseDTO response = new InventarioResponseDTO();
        response.setId(actualizado.getId());
        response.setProductoId(actualizado.getProductoId());
        response.setStockActual(actualizado.getStockActual());
        response.setStockMinimo(actualizado.getStockMinimo());

        return response; // devolvemos el objeto 
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