package com.shopconnect.ms_inventario.controller;

import java.util.List;
import java.util.Map;

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

import com.shopconnect.ms_inventario.dto.request.InventarioRequestDTO;
import com.shopconnect.ms_inventario.dto.request.MovimientoInventarioRequestDTO;
import com.shopconnect.ms_inventario.dto.response.InventarioResponseDTO;
import com.shopconnect.ms_inventario.dto.response.MovimientoInventarioResponseDTO;
import com.shopconnect.ms_inventario.service.InventarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/inventario")
public class InventarioController {

    private final InventarioService inventarioService;

    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @GetMapping
    public ResponseEntity<?> listarTodos(@RequestParam(required = false) Long productoId) {
        if (productoId != null) {
            try {
                InventarioResponseDTO inventario = inventarioService.buscarPorProductoId(productoId);
                return ResponseEntity.ok(inventario);
            } catch (RuntimeException e) {
                return ResponseEntity.notFound().build();
            }
        }

        List<InventarioResponseDTO> lista = inventarioService.listar();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            InventarioResponseDTO inventario = inventarioService.buscarPorId(id);
            return ResponseEntity.ok(inventario);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody InventarioRequestDTO dto) {
        try {
            InventarioResponseDTO creado = inventarioService.crear(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id,
                                        @Valid @RequestBody InventarioRequestDTO dto) {
        try {
            InventarioResponseDTO actualizado = inventarioService.actualizar(id, dto);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("no encontrado")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            inventarioService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{inventarioId}/stock-minimo")
    public ResponseEntity<?> ajustarStockMinimo(@PathVariable Long inventarioId,
                                                @RequestBody Map<String, Integer> body) {
        try {
            Integer stockMinimo = body.get("stockMinimo");

            if (stockMinimo == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El campo 'stockMinimo' es obligatorio"));
            }

            InventarioResponseDTO actualizado = inventarioService.ajustarStockMinimo(inventarioId, stockMinimo);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("no encontrado")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{inventarioId}/movimientos")
    public ResponseEntity<?> listarMovimientosPorInventario(@PathVariable Long inventarioId) {
        List<MovimientoInventarioResponseDTO> lista = inventarioService.listarMovimientosPorInventario(inventarioId);
        return ResponseEntity.ok(lista);
    }

    @PostMapping("/{inventarioId}/movimientos")
    public ResponseEntity<?> registrarMovimiento(@PathVariable Long inventarioId,
                                                 @Valid @RequestBody MovimientoInventarioRequestDTO dto) {
        try {
            MovimientoInventarioResponseDTO registrado = inventarioService.registrarMovimiento(inventarioId, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(registrado);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("no encontrado")) {
                return ResponseEntity.notFound().build();
            }
            if (e.getMessage() != null && e.getMessage().contains("insuficiente")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}