package com.shopconnect.ms_productos.controller;

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

import com.shopconnect.ms_productos.dto.request.CategoriaRequestDTO;
import com.shopconnect.ms_productos.dto.request.ImagenProductoRequestDTO;
import com.shopconnect.ms_productos.dto.request.MarcaRequestDTO;
import com.shopconnect.ms_productos.dto.request.ProductoRequestDTO;
import com.shopconnect.ms_productos.dto.response.CategoriaResponseDTO;
import com.shopconnect.ms_productos.dto.response.ImagenProductoResponseDTO;
import com.shopconnect.ms_productos.dto.response.MarcaResponseDTO;
import com.shopconnect.ms_productos.dto.response.ProductoResponseDTO;
import com.shopconnect.ms_productos.service.ProductoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public ResponseEntity<List<ProductoResponseDTO>> listarTodos(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long marcaId) {

        if (nombre != null && !nombre.isBlank()) {
            return ResponseEntity.ok(productoService.buscarPorNombre(nombre));
        }

        if (categoriaId != null) {
            return ResponseEntity.ok(productoService.buscarPorCategoria(categoriaId));
        }

        if (marcaId != null) {
            return ResponseEntity.ok(productoService.buscarPorMarca(marcaId));
        }

        return ResponseEntity.ok(productoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(productoService.buscarPorId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody ProductoRequestDTO dto) {
        try {
            ProductoResponseDTO creado = productoService.crear(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id,
                                        @Valid @RequestBody ProductoRequestDTO dto) {
        try {
            return ResponseEntity.ok(productoService.actualizar(id, dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            productoService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{productoId}/categoria/{categoriaId}")
    public ResponseEntity<?> actualizarCategoria(@PathVariable Long productoId,
                                                 @PathVariable Long categoriaId) {
        try {
            return ResponseEntity.ok(productoService.actualizarCategoria(productoId, categoriaId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{productoId}/marca/{marcaId}")
    public ResponseEntity<?> actualizarMarca(@PathVariable Long productoId,
                                             @PathVariable Long marcaId) {
        try {
            return ResponseEntity.ok(productoService.actualizarMarca(productoId, marcaId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/categorias")
    public ResponseEntity<List<CategoriaResponseDTO>> listarCategorias() {
        return ResponseEntity.ok(productoService.listarCategorias());
    }

    @GetMapping("/categorias/{id}")
    public ResponseEntity<?> buscarCategoriaPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(productoService.buscarCategoriaPorId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/categorias")
    public ResponseEntity<?> crearCategoria(@Valid @RequestBody CategoriaRequestDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(productoService.crearCategoria(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/categorias/{id}")
    public ResponseEntity<?> eliminarCategoria(@PathVariable Long id) {
        try {
            productoService.eliminarCategoria(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/marcas")
    public ResponseEntity<List<MarcaResponseDTO>> listarMarcas() {
        return ResponseEntity.ok(productoService.listarMarcas());
    }

    @GetMapping("/marcas/{id}")
    public ResponseEntity<?> buscarMarcaPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(productoService.buscarMarcaPorId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/marcas")
    public ResponseEntity<?> crearMarca(@Valid @RequestBody MarcaRequestDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(productoService.crearMarca(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/marcas/{id}")
    public ResponseEntity<?> eliminarMarca(@PathVariable Long id) {
        try {
            productoService.eliminarMarca(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{productoId}/imagenes")
    public ResponseEntity<List<ImagenProductoResponseDTO>> listarImagenesPorProducto(
            @PathVariable Long productoId) {
        return ResponseEntity.ok(productoService.listarImagenesPorProducto(productoId));
    }

    @PostMapping("/{productoId}/imagenes")
    public ResponseEntity<?> agregarImagen(@PathVariable Long productoId,
                                           @Valid @RequestBody ImagenProductoRequestDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(productoService.agregarImagen(productoId, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/imagenes/{id}")
    public ResponseEntity<?> eliminarImagen(@PathVariable Long id) {
        try {
            productoService.eliminarImagen(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}