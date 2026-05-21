package com.shopconnect.ms_usuarios.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.shopconnect.ms_usuarios.model.Direccion;
import com.shopconnect.ms_usuarios.model.RolUsuario;
import com.shopconnect.ms_usuarios.model.Usuario;
import com.shopconnect.ms_usuarios.service.UsuarioService;

import jakarta.validation.Valid;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * CONTROLADOR REST: UsuarioController
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Expone la API REST del microservicio ms-usuarios.
 * Todos los endpoints comienzan con: /api/v1/usuarios
 *
 * Códigos HTTP más usados:
 *   200 OK          → operación exitosa con respuesta
 *   201 Created     → recurso creado exitosamente
 *   204 No Content  → operación exitosa sin respuesta
 *   400 Bad Request → error en los datos enviados
 *   404 Not Found   → recurso no encontrado
 *   409 Conflict    → conflicto, por ejemplo email duplicado
 */
@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;
    // Spring inyecta la instancia de UsuarioService automáticamente.
    // El Controller SOLO llama al Service; no accede directamente a Repository.

    // ════════════════════════════════════════════════════════════════════════
    // ENDPOINTS DE USUARIO
    // ════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/usuarios
     * GET /api/v1/usuarios?email=correo@test.com
     * GET /api/v1/usuarios?rolId=1
     * GET /api/v1/usuarios?activo=true
     */
    @GetMapping
    public ResponseEntity<?> listarTodos(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Long rolId,
            @RequestParam(required = false) Boolean activo) {

        if (email != null && !email.isBlank()) {
            return usuarioService.buscarPorEmail(email)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }

        if (rolId != null) {
            return ResponseEntity.ok(usuarioService.listarPorRol(rolId));
        }

        if (activo != null) {
            return ResponseEntity.ok(usuarioService.listarPorActivo(activo));
        }

        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    /**
     * GET /api/v1/usuarios/1
     * Busca un usuario por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        return usuarioService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/v1/usuarios?rolId=1
     * Crea un nuevo usuario.
     */
    @PostMapping
    public ResponseEntity<?> crear(
            @Valid @RequestBody Usuario usuario,
            @RequestParam Long rolId) {
        try {
            Usuario creado = usuarioService.crear(usuario, rolId);
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
     * PUT /api/v1/usuarios/1
     * Actualiza los datos principales de un usuario.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @RequestBody Usuario datos) {
        try {
            return ResponseEntity.ok(usuarioService.actualizar(id, datos));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * DELETE /api/v1/usuarios/1
     * Elimina un usuario por su ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            usuarioService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * PATCH /api/v1/usuarios/1/activo
     * Cambia solo el estado activo del usuario.
     *
     * Body esperado:
     * { "activo": false }
     */
    @PatchMapping("/{id}/activo")
    public ResponseEntity<?> cambiarActivo(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body) {
        try {
            Boolean activo = body.get("activo");

            if (activo == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El campo 'activo' es obligatorio"));
            }

            return ResponseEntity.ok(usuarioService.cambiarActivo(id, activo));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * PATCH /api/v1/usuarios/1/rol/2
     * Cambia solo el rol asociado a un usuario.
     */
    @PatchMapping("/{usuarioId}/rol/{rolId}")
    public ResponseEntity<?> actualizarRol(
            @PathVariable Long usuarioId,
            @PathVariable Long rolId) {
        try {
            return ResponseEntity.ok(usuarioService.actualizarRol(usuarioId, rolId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // ENDPOINTS DE ROL DE USUARIO
    // ════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/usuarios/roles
     * Lista todos los roles.
     */
    @GetMapping("/roles")
    public ResponseEntity<List<RolUsuario>> listarRoles() {
        return ResponseEntity.ok(usuarioService.listarRoles());
    }

    /**
     * GET /api/v1/usuarios/roles/1
     * Busca un rol por ID.
     */
    @GetMapping("/roles/{id}")
    public ResponseEntity<?> buscarRolPorId(@PathVariable Long id) {
        return usuarioService.buscarRolPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/v1/usuarios/roles
     * Crea un nuevo rol.
     */
    @PostMapping("/roles")
    public ResponseEntity<?> crearRol(@Valid @RequestBody RolUsuario rol) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(usuarioService.crearRol(rol));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/v1/usuarios/roles/1
     * Elimina un rol por su ID.
     */
    @DeleteMapping("/roles/{id}")
    public ResponseEntity<?> eliminarRol(@PathVariable Long id) {
        try {
            usuarioService.eliminarRol(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // ENDPOINTS DE DIRECCIONES
    // ════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/usuarios/1/direcciones
     * Lista las direcciones asociadas a un usuario.
     */
    @GetMapping("/{usuarioId}/direcciones")
    public ResponseEntity<List<Direccion>> listarDireccionesPorUsuario(
            @PathVariable Long usuarioId) {
        return ResponseEntity.ok(usuarioService.listarDireccionesPorUsuario(usuarioId));
    }

    /**
     * POST /api/v1/usuarios/1/direcciones
     * Agrega una dirección a un usuario.
     */
    @PostMapping("/{usuarioId}/direcciones")
    public ResponseEntity<?> agregarDireccion(
            @PathVariable Long usuarioId,
            @Valid @RequestBody Direccion direccion) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(usuarioService.agregarDireccion(usuarioId, direccion));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/v1/usuarios/direcciones/1
     * Elimina una dirección por su ID.
     */
    @DeleteMapping("/direcciones/{id}")
    public ResponseEntity<?> eliminarDireccion(@PathVariable Long id) {
        try {
            usuarioService.eliminarDireccion(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}