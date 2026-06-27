package com.shopconnect.ms_pedidos.controller;

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

import com.shopconnect.ms_pedidos.dto.request.DetallePedidoRequestDTO;
import com.shopconnect.ms_pedidos.dto.request.EstadoPedidoRequestDTO;
import com.shopconnect.ms_pedidos.dto.request.PedidoRequestDTO;
import com.shopconnect.ms_pedidos.dto.response.DetallePedidoResponseDTO;
import com.shopconnect.ms_pedidos.dto.response.EstadoPedidoResponseDTO;
import com.shopconnect.ms_pedidos.dto.response.PedidoResponseDTO;
import com.shopconnect.ms_pedidos.service.PedidoService;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/pedidos")
@Tag(name = "Pedidos", description = "Operaciones para pedidos, estados y detalles")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @Operation(summary = "Listar pedidos", description = "Lista todos los pedidos o filtra por usuarioId o estadoId.")
    @GetMapping
    public ResponseEntity<List<PedidoResponseDTO>> listarTodos(
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

    @Operation(summary = "Buscar pedido por ID")
    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> buscarPorId(@PathVariable Long id) {
        return pedidoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Crear pedido")
    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody PedidoRequestDTO dto) {
        try {
            PedidoResponseDTO creado = pedidoService.crear(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(java.util.Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Actualizar pedido")
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id,
                                       @Valid @RequestBody PedidoRequestDTO dto) {
        try {
            return ResponseEntity.ok(pedidoService.actualizar(id, dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(java.util.Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Eliminar pedido")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            pedidoService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Cambiar estado del pedido")
    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id,
                                           @RequestParam Long estadoId) {
        try {
            return ResponseEntity.ok(pedidoService.cambiarEstado(id, estadoId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Listar estados de pedido")
    @GetMapping("/estados")
    public ResponseEntity<List<EstadoPedidoResponseDTO>> listarEstados() {
        return ResponseEntity.ok(pedidoService.listarEstados());
    }

    @Operation(summary = "Buscar estado por ID")
    @GetMapping("/estados/{id}")
    public ResponseEntity<EstadoPedidoResponseDTO> buscarEstadoPorId(@PathVariable Long id) {
        return pedidoService.buscarEstadoPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Crear estado de pedido")
    @PostMapping("/estados")
    public ResponseEntity<?> crearEstado(@Valid @RequestBody EstadoPedidoRequestDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(pedidoService.crearEstado(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Eliminar estado de pedido")
    @DeleteMapping("/estados/{id}")
    public ResponseEntity<?> eliminarEstado(@PathVariable Long id) {
        try {
            pedidoService.eliminarEstado(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(java.util.Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Listar detalles por pedido")
    @GetMapping("/{pedidoId}/detalles")
    public ResponseEntity<List<DetallePedidoResponseDTO>> listarDetallesPorPedido(
            @PathVariable Long pedidoId) {
        return ResponseEntity.ok(pedidoService.listarDetallesPorPedido(pedidoId));
    }

    @Operation(summary = "Listar detalles por producto")
    @GetMapping("/detalles/producto/{productoId}")
    public ResponseEntity<List<DetallePedidoResponseDTO>> listarDetallesPorProducto(
            @PathVariable Long productoId) {
        return ResponseEntity.ok(pedidoService.listarDetallesPorProducto(productoId));
    }

    @Operation(summary = "Agregar detalle a un pedido")
    @PostMapping("/{pedidoId}/detalles")
    public ResponseEntity<?> agregarDetalle(@PathVariable Long pedidoId,
                                            @Valid @RequestBody DetallePedidoRequestDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(pedidoService.agregarDetalle(pedidoId, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Eliminar detalle de pedido")
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