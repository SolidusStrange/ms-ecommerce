package com.shopconnect.ms_pagos.controller;

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

import com.shopconnect.ms_pagos.model.MetodoPago;
import com.shopconnect.ms_pagos.model.Pago;
import com.shopconnect.ms_pagos.model.TransaccionPago;
import com.shopconnect.ms_pagos.service.PagoService;

import jakarta.validation.Valid;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * CONTROLADOR REST: PagoController
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Expone la API REST del microservicio ms-pagos.
 * Todos los endpoints comienzan con: /api/v1/pagos
 *
 * Códigos HTTP más usados:
 *   200 OK          → operación exitosa con respuesta
 *   201 Created     → recurso creado exitosamente
 *   204 No Content  → operación exitosa sin respuesta
 *   400 Bad Request → error en los datos enviados
 *   404 Not Found   → recurso no encontrado
 *   409 Conflict    → conflicto, por ejemplo método duplicado
 */
@RestController
@RequestMapping("/api/v1/pagos")
public class PagoController {

    @Autowired
    private PagoService pagoService;
    // Spring inyecta la instancia de PagoService automáticamente.
    // El Controller SOLO llama al Service; no accede directamente a Repository.

    // ════════════════════════════════════════════════════════════════════════
    // ENDPOINTS DE PAGO
    // ════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/pagos
     * GET /api/v1/pagos?pedidoId=1
     * GET /api/v1/pagos?estado=PENDIENTE
     */
    @GetMapping
    public ResponseEntity<?> listarTodos(
            @RequestParam(required = false) Long pedidoId,
            @RequestParam(required = false) String estado) {

        if (pedidoId != null) {
            return ResponseEntity.ok(pagoService.listarPorPedido(pedidoId));
        }

        if (estado != null && !estado.isBlank()) {
            return ResponseEntity.ok(pagoService.listarPorEstado(estado));
        }

        return ResponseEntity.ok(pagoService.listarTodos());
    }

    /**
     * GET /api/v1/pagos/1
     * Busca un pago por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        return pagoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/v1/pagos?metodoId=1
     * Procesa un nuevo pago.
     *
     * Body esperado:
     * {
     *   "monto": 499990,
     *   "pedidoId": 1
     * }
     */
    @PostMapping
    public ResponseEntity<?> procesar(
            @Valid @RequestBody Pago pago,
            @RequestParam Long metodoId) {
        try {
            Pago procesado = pagoService.procesar(pago, metodoId);
            return ResponseEntity.status(HttpStatus.CREATED).body(procesado);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/v1/pagos/1
     * Actualiza datos principales de un pago.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @RequestBody Pago datos) {
        try {
            return ResponseEntity.ok(pagoService.actualizar(id, datos));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * DELETE /api/v1/pagos/1
     * Elimina un pago por ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            pagoService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * PATCH /api/v1/pagos/1/estado?estado=APROBADO
     * Cambia solo el estado del pago.
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstado(
            @PathVariable Long id,
            @RequestParam String estado) {
        try {
            return ResponseEntity.ok(pagoService.actualizarEstado(id, estado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // ENDPOINTS DE MÉTODO DE PAGO
    // ════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/pagos/metodos
     * Lista todos los métodos de pago.
     */
    @GetMapping("/metodos")
    public ResponseEntity<List<MetodoPago>> listarMetodos() {
        return ResponseEntity.ok(pagoService.listarMetodos());
    }

    /**
     * GET /api/v1/pagos/metodos/activos
     * Lista solo los métodos de pago activos.
     */
    @GetMapping("/metodos/activos")
    public ResponseEntity<List<MetodoPago>> listarMetodosActivos() {
        return ResponseEntity.ok(pagoService.listarMetodosActivos());
    }

    /**
     * GET /api/v1/pagos/metodos/1
     * Busca un método de pago por ID.
     */
    @GetMapping("/metodos/{id}")
    public ResponseEntity<?> buscarMetodoPorId(@PathVariable Long id) {
        return pagoService.buscarMetodoPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/v1/pagos/metodos
     * Crea un nuevo método de pago.
     *
     * Body esperado:
     * {
     *   "nombre": "TARJETA",
     *   "activo": true
     * }
     */
    @PostMapping("/metodos")
    public ResponseEntity<?> crearMetodo(@Valid @RequestBody MetodoPago metodoPago) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(pagoService.crearMetodo(metodoPago));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/v1/pagos/metodos/1
     * Actualiza un método de pago.
     */
    @PutMapping("/metodos/{id}")
    public ResponseEntity<?> actualizarMetodo(
            @PathVariable Long id,
            @RequestBody MetodoPago datos) {
        try {
            return ResponseEntity.ok(pagoService.actualizarMetodo(id, datos));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * DELETE /api/v1/pagos/metodos/1
     * Elimina un método de pago.
     */
    @DeleteMapping("/metodos/{id}")
    public ResponseEntity<?> eliminarMetodo(@PathVariable Long id) {
        try {
            pagoService.eliminarMetodo(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // ENDPOINTS DE TRANSACCIONES DE PAGO
    // ════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/pagos/1/transacciones
     * Lista las transacciones asociadas a un pago.
     */
    @GetMapping("/{pagoId}/transacciones")
    public ResponseEntity<List<TransaccionPago>> listarTransaccionesPorPago(
            @PathVariable Long pagoId) {
        return ResponseEntity.ok(pagoService.listarTransaccionesPorPago(pagoId));
    }

    /**
     * POST /api/v1/pagos/1/transacciones
     * Agrega una transacción a un pago existente.
     *
     * Body esperado:
     * {
     *   "codigoTransaccion": "TX-EXTERNA-001",
     *   "estado": "APROBADO"
     * }
     */
    @PostMapping("/{pagoId}/transacciones")
    public ResponseEntity<?> agregarTransaccion(
            @PathVariable Long pagoId,
            @Valid @RequestBody TransaccionPago transaccion) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(pagoService.agregarTransaccion(pagoId, transaccion));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/v1/pagos/transacciones/1
     * Elimina una transacción por ID.
     */
    @DeleteMapping("/transacciones/{id}")
    public ResponseEntity<?> eliminarTransaccion(@PathVariable Long id) {
        try {
            pagoService.eliminarTransaccion(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}