package com.shopconnect.ms_usuarios.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.shopconnect.ms_usuarios.dto.DireccionDTO;
import com.shopconnect.ms_usuarios.dto.RolUsuarioDTO;
import com.shopconnect.ms_usuarios.dto.UsuarioDTO;
import com.shopconnect.ms_usuarios.dto.request.DireccionRequestDTO;
import com.shopconnect.ms_usuarios.dto.request.RolUsuarioRequestDTO;
import com.shopconnect.ms_usuarios.dto.request.UsuarioRequestDTO;
import com.shopconnect.ms_usuarios.service.UsuarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<?> listarTodos(@RequestParam(required = false) String email,
                                         @RequestParam(required = false) Long rolId,
                                         @RequestParam(required = false) Boolean activo) {
        if (email != null && !email.isBlank()) {
            try {
                return ResponseEntity.ok(usuarioService.buscarPorEmail(email));
            } catch (RuntimeException e) {
                return ResponseEntity.notFound().build();
            }
        }

        if (rolId != null) {
            return ResponseEntity.ok(usuarioService.listarPorRol(rolId));
        }

        if (activo != null) {
            return ResponseEntity.ok(usuarioService.listarPorActivo(activo));
        }

        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(usuarioService.buscarPorId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody UsuarioRequestDTO dto) {
        try {
            UsuarioDTO creado = usuarioService.crear(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequestDTO dto) {
        try {
            return ResponseEntity.ok(usuarioService.actualizar(id, dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            usuarioService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/activo")
    public ResponseEntity<?> cambiarActivo(@PathVariable Long id,
                                           @RequestBody Map<String, Boolean> body) {
        try {
            Boolean activo = body.get("activo");
            if (activo == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "El campo 'activo' es obligatorio"));
            }
            return ResponseEntity.ok(usuarioService.cambiarActivo(id, activo));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{usuarioId}/rol/{rolId}")
    public ResponseEntity<?> actualizarRol(@PathVariable Long usuarioId, @PathVariable Long rolId) {
        try {
            return ResponseEntity.ok(usuarioService.actualizarRol(usuarioId, rolId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/roles")
    public ResponseEntity<List<RolUsuarioDTO>> listarRoles() {
        return ResponseEntity.ok(usuarioService.listarRoles());
    }

    @GetMapping("/roles/{id}")
    public ResponseEntity<?> buscarRolPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(usuarioService.buscarRolPorId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/roles")
    public ResponseEntity<?> crearRol(@Valid @RequestBody RolUsuarioRequestDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crearRol(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/roles/{id}")
    public ResponseEntity<?> eliminarRol(@PathVariable Long id) {
        try {
            usuarioService.eliminarRol(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{usuarioId}/direcciones")
    public ResponseEntity<List<DireccionDTO>> listarDireccionesPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(usuarioService.listarDireccionesPorUsuario(usuarioId));
    }

    @PostMapping("/{usuarioId}/direcciones")
    public ResponseEntity<?> agregarDireccion(@PathVariable Long usuarioId,
                                              @Valid @RequestBody DireccionRequestDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.agregarDireccion(usuarioId, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

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