package com.shopconnect.ms_inventario.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shopconnect.ms_inventario.model.Inventario;
import com.shopconnect.ms_inventario.model.MovimientoInventario;
import com.shopconnect.ms_inventario.service.InventarioService;

import jakarta.validation.Valid;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * CONTROLADOR REST: InventarioController
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Expone la API REST del microservicio ms-inventario.
 * Todos los endpoints comienzan con: /api/v1/inventario
 *
 * Códigos HTTP más usados:
 *   200 OK          → operación exitosa con respuesta
 *   201 Created     → recurso creado exitosamente
 *   204 No Content  → operación exitosa sin respuesta
 *   400 Bad Request → error en los datos enviados
 *   404 Not Found   → recurso no encontrado
 *   409 Conflict    → conflicto, por ejemplo inventario duplicado
 */
@RestController
@RequestMapping("/api/v1/inventario")
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;
    // Spring inyecta la instancia de InventarioService automáticamente.
    // El Controller SOLO llama al Service; no accede directamente a Repository.

    // ════════════════════════════════════════════════════════════════════════
    // ENDPOINTS DE INVENTARIO
    // ════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/inventario
     * GET /api/v1/inventario?productoId=1
     */
    @GetMapping
    public ResponseEntity<?> listarTodos(
            @RequestParam(required = false) Long productoId) {

        if (productoId != null) {
            return inventarioService.buscarPorProductoId(productoId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }

        return ResponseEntity.ok(inventarioService.listarTodos());
    }

    /**
     * GET /api/v1/inventario/1
     * Busca un inventario por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        return inventarioService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/v1/inventario/producto/1
     * Busca inventario por productoId.
     */
    @GetMapping("/producto/{productoId}")
    public ResponseEntity<?> buscarPorProductoId(@PathVariable Long productoId) {
        return inventarioService.buscarPorProductoId(productoId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/v1/inventario
     * Crea un inventario para un producto.
     *
     * Body esperado:
     * {
     *   "productoId": 1,
     *   "stockActual": 20,
     *   "stockMinimo": 5
     * }
     */
    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody Inventario inventario) {
        try {
            Inventario creado = inventarioService.crear(inventario);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/v1/inventario/1
     * Actualiza los datos principales de un inventario.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @RequestBody Inventario datos) {
        try {
            return ResponseEntity.ok(inventarioService.actualizar(id, datos));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * DELETE /api/v1/inventario/1
     * Elimina un inventario por su ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            inventarioService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * PATCH /api/v1/inventario/1/stock-minimo
     * Cambia solo el stock mínimo.
     *
     * Body esperado:
     * { "stockMinimo": 5 }
     */
    @PatchMapping("/{inventarioId}/stock-minimo")
    public ResponseEntity<?> ajustarStockMinimo(
            @PathVariable Long inventarioId,
            @RequestBody Map<String, Integer> body) {
        try {
            Integer stockMinimo = body.get("stockMinimo");

            if (stockMinimo == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El campo 'stockMinimo' es obligatorio"));
            }

            return ResponseEntity.ok(inventarioService.ajustarStockMinimo(inventarioId, stockMinimo));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // ENDPOINTS DE MOVIMIENTOS DE INVENTARIO
    // ════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/inventario/1/movimientos
     * Lista los movimientos asociados a un inventario.
     */
    @GetMapping("/{inventarioId}/movimientos")
    public ResponseEntity<List<MovimientoInventario>> listarMovimientosPorInventario(
            @PathVariable Long inventarioId) {
        return ResponseEntity.ok(inventarioService.listarMovimientosPorInventario(inventarioId));
    }

    /**
     * POST /api/v1/inventario/1/movimientos
     * Registra un movimiento de inventario.
     *
     * Body esperado para entrada:
     * {
     *   "tipo": "ENTRADA",
     *   "cantidad": 10
     * }
     *
     * Body esperado para salida:
     * {
     *   "tipo": "SALIDA",
     *   "cantidad": 3
     * }
     */
    @PostMapping("/{inventarioId}/movimientos")
    public ResponseEntity<?> registrarMovimiento(
            @PathVariable Long inventarioId,
            @Valid @RequestBody MovimientoInventario movimiento) {
        try {
            MovimientoInventario registrado = inventarioService.registrarMovimiento(inventarioId, movimiento);
            return ResponseEntity.status(HttpStatus.CREATED).body(registrado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}