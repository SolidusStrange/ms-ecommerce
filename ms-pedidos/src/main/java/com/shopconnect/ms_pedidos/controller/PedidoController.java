package com.shopconnect.ms_pedidos.controller;

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

import com.shopconnect.ms_pedidos.model.DetallePedido;
import com.shopconnect.ms_pedidos.model.EstadoPedido;
import com.shopconnect.ms_pedidos.model.Pedido;
import com.shopconnect.ms_pedidos.service.PedidoService;

import jakarta.validation.Valid;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * CONTROLADOR REST: PedidoController
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Expone la API REST del microservicio ms-pedidos.
 * Todos los endpoints comienzan con: /api/v1/pedidos
 *
 * Códigos HTTP más usados:
 *   200 OK          → operación exitosa con respuesta
 *   201 Created     → recurso creado exitosamente
 *   204 No Content  → operación exitosa sin respuesta
 *   400 Bad Request → error en los datos enviados
 *   404 Not Found   → recurso no encontrado
 *   409 Conflict    → conflicto, por ejemplo estado duplicado
 */
@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;
    // Spring inyecta la instancia de PedidoService automáticamente.
    // El Controller SOLO llama al Service; no accede directamente a Repository.

    // ════════════════════════════════════════════════════════════════════════
    // ENDPOINTS DE PEDIDO
    // ════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/pedidos
     * GET /api/v1/pedidos?usuarioId=1
     * GET /api/v1/pedidos?estadoId=1
     */
    @GetMapping
    public ResponseEntity<List<Pedido>> listarTodos(
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) Long estadoId) {

        if (usuarioId != null) {
            return ResponseEntity.ok(pedidoService.listarPorUsuario(usuarioId));
        }

        if (estadoId != null) {
            return ResponseEntity.ok(pedidoService.listarPorEstado(estadoId));
        }

        return ResponseEntity.ok(pedidoService.listarTodos());
    }

    /**
     * GET /api/v1/pedidos/1
     * Busca un pedido por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        return pedidoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/v1/pedidos?estadoId=1
     * Crea un nuevo pedido.
     *
     * Body esperado:
     * {
     *   "usuarioId": 1,
     *   "detalles": [
     *     {
     *       "productoId": 1,
     *       "cantidad": 2,
     *       "precioUnit": 499990
     *     }
     *   ]
     * }
     */
    @PostMapping
    public ResponseEntity<?> crear(
            @Valid @RequestBody Pedido pedido,
            @RequestParam Long estadoId) {
        try {
            Pedido creado = pedidoService.crear(pedido, estadoId);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/v1/pedidos/1
     * Actualiza datos principales del pedido.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @RequestBody Pedido datos) {
        try {
            return ResponseEntity.ok(pedidoService.actualizar(id, datos));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * DELETE /api/v1/pedidos/1
     * Elimina un pedido por ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            pedidoService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * PATCH /api/v1/pedidos/1/estado?estadoId=2
     * Cambia solo el estado del pedido.
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(
            @PathVariable Long id,
            @RequestParam Long estadoId) {
        try {
            return ResponseEntity.ok(pedidoService.cambiarEstado(id, estadoId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // ENDPOINTS DE ESTADO DE PEDIDO
    // ════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/pedidos/estados
     * Lista todos los estados de pedido.
     */
    @GetMapping("/estados")
    public ResponseEntity<List<EstadoPedido>> listarEstados() {
        return ResponseEntity.ok(pedidoService.listarEstados());
    }

    /**
     * GET /api/v1/pedidos/estados/1
     * Busca un estado por ID.
     */
    @GetMapping("/estados/{id}")
    public ResponseEntity<?> buscarEstadoPorId(@PathVariable Long id) {
        return pedidoService.buscarEstadoPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/v1/pedidos/estados
     * Crea un nuevo estado de pedido.
     */
    @PostMapping("/estados")
    public ResponseEntity<?> crearEstado(@Valid @RequestBody EstadoPedido estado) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(pedidoService.crearEstado(estado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/v1/pedidos/estados/1
     * Elimina un estado de pedido.
     */
    @DeleteMapping("/estados/{id}")
    public ResponseEntity<?> eliminarEstado(@PathVariable Long id) {
        try {
            pedidoService.eliminarEstado(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // ENDPOINTS DE DETALLE DE PEDIDO
    // ════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/pedidos/1/detalles
     * Lista los detalles asociados a un pedido.
     */
    @GetMapping("/{pedidoId}/detalles")
    public ResponseEntity<List<DetallePedido>> listarDetallesPorPedido(
            @PathVariable Long pedidoId) {
        return ResponseEntity.ok(pedidoService.listarDetallesPorPedido(pedidoId));
    }

    /**
     * GET /api/v1/pedidos/detalles/producto/1
     * Lista detalles donde aparece un producto específico.
     */
    @GetMapping("/detalles/producto/{productoId}")
    public ResponseEntity<List<DetallePedido>> listarDetallesPorProducto(
            @PathVariable Long productoId) {
        return ResponseEntity.ok(pedidoService.listarDetallesPorProducto(productoId));
    }

    /**
     * POST /api/v1/pedidos/1/detalles
     * Agrega un detalle a un pedido existente y recalcula el total.
     */
    @PostMapping("/{pedidoId}/detalles")
    public ResponseEntity<?> agregarDetalle(
            @PathVariable Long pedidoId,
            @Valid @RequestBody DetallePedido detalle) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(pedidoService.agregarDetalle(pedidoId, detalle));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/v1/pedidos/detalles/1
     * Elimina un detalle y recalcula el total del pedido.
     */
    @DeleteMapping("/detalles/{id}")
    public ResponseEntity<?> eliminarDetalle(@PathVariable Long id) {
        try {
            pedidoService.eliminarDetalle(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}