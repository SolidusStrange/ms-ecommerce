package com.shopconnect.ms_pagos.controller;

import java.util.List;

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

import com.shopconnect.ms_pagos.dto.request.MetodoPagoRequestDTO;
import com.shopconnect.ms_pagos.dto.request.PagoRequestDTO;
import com.shopconnect.ms_pagos.dto.request.TransaccionPagoRequestDTO;
import com.shopconnect.ms_pagos.dto.response.MetodoPagoResponseDTO;
import com.shopconnect.ms_pagos.dto.response.PagoResponseDTO;
import com.shopconnect.ms_pagos.dto.response.TransaccionPagoResponseDTO;
import com.shopconnect.ms_pagos.service.PagoService;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/pagos")
@Tag(name = "Pagos", description = "Operaciones para pagos, métodos de pago y transacciones")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @Operation(summary = "Listar pagos", description = "Lista todos los pagos o filtra por pedidoId o estado.")
    @GetMapping
    public ResponseEntity<List<PagoResponseDTO>> listarTodos(
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

    @Operation(summary = "Buscar pago por ID")
    @GetMapping("/{id}")
    public ResponseEntity<PagoResponseDTO> buscarPorId(@PathVariable Long id) {
        return pagoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Procesar pago")
    @PostMapping
    public ResponseEntity<?> procesar(@Valid @RequestBody PagoRequestDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(pagoService.procesar(dto));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(java.util.Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Actualizar pago")
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id,
                                        @Valid @RequestBody PagoRequestDTO dto) {
        try {
            return ResponseEntity.ok(pagoService.actualizar(id, dto));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(java.util.Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Eliminar pago")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            pagoService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Actualizar estado de pago")
    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstado(@PathVariable Long id,
                                              @RequestParam String estado) {
        try {
            return ResponseEntity.ok(pagoService.actualizarEstado(id, estado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Listar métodos de pago")
    @GetMapping("/metodos")
    public ResponseEntity<List<MetodoPagoResponseDTO>> listarMetodos() {
        return ResponseEntity.ok(pagoService.listarMetodos());
    }

    @Operation(summary = "Listar métodos de pago activos")
    @GetMapping("/metodos/activos")
    public ResponseEntity<List<MetodoPagoResponseDTO>> listarMetodosActivos() {
        return ResponseEntity.ok(pagoService.listarMetodosActivos());
    }

    @Operation(summary = "Buscar método de pago por ID")
    @GetMapping("/metodos/{id}")
    public ResponseEntity<MetodoPagoResponseDTO> buscarMetodoPorId(@PathVariable Long id) {
        return pagoService.buscarMetodoPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Crear método de pago")
    @PostMapping("/metodos")
    public ResponseEntity<?> crearMetodo(@Valid @RequestBody MetodoPagoRequestDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(pagoService.crearMetodo(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Actualizar método de pago")
    @PutMapping("/metodos/{id}")
    public ResponseEntity<?> actualizarMetodo(@PathVariable Long id,
                                              @Valid @RequestBody MetodoPagoRequestDTO dto) {
        try {
            return ResponseEntity.ok(pagoService.actualizarMetodo(id, dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(java.util.Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Eliminar método de pago")
    @DeleteMapping("/metodos/{id}")
    public ResponseEntity<?> eliminarMetodo(@PathVariable Long id) {
        try {
            pagoService.eliminarMetodo(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Listar transacciones por pago")
    @GetMapping("/{pagoId}/transacciones")
    public ResponseEntity<List<TransaccionPagoResponseDTO>> listarTransaccionesPorPago(
            @PathVariable Long pagoId) {
        return ResponseEntity.ok(pagoService.listarTransaccionesPorPago(pagoId));
    }

    @Operation(summary = "Agregar transacción a un pago")
    @PostMapping("/{pagoId}/transacciones")
    public ResponseEntity<?> agregarTransaccion(@PathVariable Long pagoId,
                                                @Valid @RequestBody TransaccionPagoRequestDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(pagoService.agregarTransaccion(pagoId, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Eliminar transacción")
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