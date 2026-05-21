package com.shopconnect.ms_productos.controller;

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

import com.shopconnect.ms_productos.model.Categoria;
import com.shopconnect.ms_productos.model.ImagenProducto;
import com.shopconnect.ms_productos.model.Marca;
import com.shopconnect.ms_productos.model.Producto;
import com.shopconnect.ms_productos.service.ProductoService;

import jakarta.validation.Valid;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * CONTROLADOR REST: ProductoController
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Expone la API REST del microservicio ms-productos.
 * Todos los endpoints comienzan con: /api/v1/productos
 *
 * Códigos HTTP más usados:
 *   200 OK          → operación exitosa con respuesta
 *   201 Created     → recurso creado exitosamente
 *   204 No Content  → operación exitosa sin respuesta
 *   400 Bad Request → error en los datos enviados
 *   404 Not Found   → recurso no encontrado
 *   409 Conflict    → conflicto, por ejemplo SKU duplicado
 */
@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;
    // Spring inyecta la instancia de ProductoService automáticamente.
    // El Controller SOLO llama al Service; no accede directamente a Repository.

    // ════════════════════════════════════════════════════════════════════════
    // ENDPOINTS DE PRODUCTO
    // ════════════════════════════════════════════════════════════════════════
    
    /**
     * GET /api/v1/productos
     * GET /api/v1/productos?nombre=notebook
     * GET /api/v1/productos?categoriaId=1
     * GET /api/v1/productos?marcaId=1
     */
    @GetMapping
    public ResponseEntity<List<Producto>> listarTodos(
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

    /**
     * GET /api/v1/productos/1
     * Busca un producto por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        return productoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/v1/productos/categoria/1
     * Lista productos filtrados por categoría.
     */
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<Producto>> listarPorCategoria(@PathVariable Long categoriaId) {
        return ResponseEntity.ok(productoService.buscarPorCategoria(categoriaId));
    }

    /**
     * GET /api/v1/productos/marca/1
     * Lista productos filtrados por marca.
     */
    @GetMapping("/marca/{marcaId}")
    public ResponseEntity<List<Producto>> listarPorMarca(@PathVariable Long marcaId) {
        return ResponseEntity.ok(productoService.buscarPorMarca(marcaId));
    }

    /**
     * GET /api/v1/productos/buscar?nombre=notebook
     * Busca productos por coincidencia parcial del nombre.
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<Producto>> buscarPorNombre(@RequestParam String nombre) {
        return ResponseEntity.ok(productoService.buscarPorNombre(nombre));
    }

    /**
     * POST /api/v1/productos?categoriaId=1&marcaId=1
     * Crea un nuevo producto.
     */
    @PostMapping
    public ResponseEntity<?> crear(
            @Valid @RequestBody Producto producto,
            @RequestParam Long categoriaId,
            @RequestParam Long marcaId) {
        try {
            Producto creado = productoService.crear(producto, categoriaId, marcaId);
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
     * PUT /api/v1/productos/1
     * Actualiza los datos principales de un producto.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @RequestBody Producto datos) {
        try {
            return ResponseEntity.ok(productoService.actualizar(id, datos));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * DELETE /api/v1/productos/1
     * Elimina un producto por su ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            productoService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * PATCH /api/v1/productos/1/categoria/2
     * Cambia solo la categoría asociada a un producto.
     */
    @PatchMapping("/{productoId}/categoria/{categoriaId}")
    public ResponseEntity<?> actualizarCategoria(
            @PathVariable Long productoId,
            @PathVariable Long categoriaId) {
        try {
            return ResponseEntity.ok(productoService.actualizarCategoria(productoId, categoriaId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * PATCH /api/v1/productos/1/marca/2
     * Cambia solo la marca asociada a un producto.
     */
    @PatchMapping("/{productoId}/marca/{marcaId}")
    public ResponseEntity<?> actualizarMarca(
            @PathVariable Long productoId,
            @PathVariable Long marcaId) {
        try {
            return ResponseEntity.ok(productoService.actualizarMarca(productoId, marcaId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // ENDPOINTS DE CATEGORIA
    // ════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/productos/categorias
     * Lista todas las categorías.
     */
    @GetMapping("/categorias")
    public ResponseEntity<List<Categoria>> listarCategorias() {
        return ResponseEntity.ok(productoService.listarCategorias());
    }

    /**
     * GET /api/v1/productos/categorias/1
     * Busca una categoría por ID.
     */
    @GetMapping("/categorias/{id}")
    public ResponseEntity<?> buscarCategoriaPorId(@PathVariable Long id) {
        return productoService.buscarCategoriaPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/v1/productos/categorias
     * Crea una nueva categoría.
     */
    @PostMapping("/categorias")
    public ResponseEntity<?> crearCategoria(@Valid @RequestBody Categoria categoria) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(productoService.crearCategoria(categoria));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/v1/productos/categorias/1
     * Elimina una categoría por su ID.
     */
    @DeleteMapping("/categorias/{id}")
    public ResponseEntity<?> eliminarCategoria(@PathVariable Long id) {
        try {
            productoService.eliminarCategoria(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // ENDPOINTS DE MARCA
    // ════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/productos/marcas
     * Lista todas las marcas.
     */
    @GetMapping("/marcas")
    public ResponseEntity<List<Marca>> listarMarcas() {
        return ResponseEntity.ok(productoService.listarMarcas());
    }

    /**
     * GET /api/v1/productos/marcas/1
     * Busca una marca por ID.
     */
    @GetMapping("/marcas/{id}")
    public ResponseEntity<?> buscarMarcaPorId(@PathVariable Long id) {
        return productoService.buscarMarcaPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/v1/productos/marcas
     * Crea una nueva marca.
     */
    @PostMapping("/marcas")
    public ResponseEntity<?> crearMarca(@Valid @RequestBody Marca marca) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(productoService.crearMarca(marca));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/v1/productos/marcas/1
     * Elimina una marca por su ID.
     */
    @DeleteMapping("/marcas/{id}")
    public ResponseEntity<?> eliminarMarca(@PathVariable Long id) {
        try {
            productoService.eliminarMarca(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // ENDPOINTS DE IMAGENES DE PRODUCTO
    // ════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/productos/1/imagenes
     * Lista las imágenes asociadas a un producto.
     */
    @GetMapping("/{productoId}/imagenes")
    public ResponseEntity<List<ImagenProducto>> listarImagenesPorProducto(
            @PathVariable Long productoId) {
        return ResponseEntity.ok(productoService.listarImagenesPorProducto(productoId));
    }

    /**
     * POST /api/v1/productos/1/imagenes
     * Agrega una imagen a un producto.
     */
    @PostMapping("/{productoId}/imagenes")
    public ResponseEntity<?> agregarImagen(
            @PathVariable Long productoId,
            @Valid @RequestBody ImagenProducto imagenProducto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(productoService.agregarImagen(productoId, imagenProducto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/v1/productos/imagenes/1
     * Elimina una imagen por su ID.
     */
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