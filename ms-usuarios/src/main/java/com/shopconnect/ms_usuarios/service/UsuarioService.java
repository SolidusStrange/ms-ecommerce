package com.shopconnect.ms_usuarios.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopconnect.ms_usuarios.model.Direccion;
import com.shopconnect.ms_usuarios.model.RolUsuario;
import com.shopconnect.ms_usuarios.model.Usuario;
import com.shopconnect.ms_usuarios.repository.DireccionRepository;
import com.shopconnect.ms_usuarios.repository.RolUsuarioRepository;
import com.shopconnect.ms_usuarios.repository.UsuarioRepository;

/**
 * SERVICIO: UsuarioService
 *
 * RESPONSABILIDAD: Lógica de negocio.
 * El Service llama a los métodos del Repository.
 * El Service NO escribe SQL ni usa EntityManager directamente.
 */
@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolUsuarioRepository rolUsuarioRepository;

    @Autowired
    private DireccionRepository direccionRepository;

    // ═══ USUARIOS ════════════════════════════════════════════════════

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public List<Usuario> listarPorRol(Long rolId) {
        return usuarioRepository.findByRolId(rolId);
    }

    public List<Usuario> listarPorActivo(Boolean activo) {
        return usuarioRepository.findByActivo(activo);
    }

    @Transactional
    public Usuario crear(Usuario usuario, Long rolId) {

        // Validar email único
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new IllegalArgumentException("Email duplicado: " + usuario.getEmail());
        }

        // Validar que existe el rol
        RolUsuario rol = rolUsuarioRepository.findById(rolId)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + rolId));

        usuario.setRol(rol);

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario actualizar(Long id, Usuario datos) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + id));

        if (datos.getNombre() != null) {
            usuario.setNombre(datos.getNombre());
        }

        if (datos.getEmail() != null) {
            if (!datos.getEmail().equals(usuario.getEmail())
                    && usuarioRepository.existsByEmail(datos.getEmail())) {
                throw new IllegalArgumentException("Email duplicado: " + datos.getEmail());
            }

            usuario.setEmail(datos.getEmail());
        }

        if (datos.getPassword() != null) {
            usuario.setPassword(datos.getPassword());
        }

        if (datos.getActivo() != null) {
            usuario.setActivo(datos.getActivo());
        }

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void eliminar(Long id) {

        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado: " + id);
        }

        usuarioRepository.deleteById(id);
    }

    // ═══ ROLES DE USUARIO ═════════════════════════════════════════════

    public List<RolUsuario> listarRoles() {
        return rolUsuarioRepository.findAll();
    }

    public Optional<RolUsuario> buscarRolPorId(Long id) {
        return rolUsuarioRepository.findById(id);
    }

    @Transactional
    public RolUsuario crearRol(RolUsuario rol) {

        if (rolUsuarioRepository.existsByNombre(rol.getNombre())) {
            throw new IllegalArgumentException("Nombre de rol duplicado: " + rol.getNombre());
        }

        return rolUsuarioRepository.save(rol);
    }

    @Transactional
    public void eliminarRol(Long id) {

        if (!rolUsuarioRepository.existsById(id)) {
            throw new RuntimeException("Rol no encontrado: " + id);
        }

        List<Usuario> usuariosConRol = usuarioRepository.findByRolId(id);

        if (!usuariosConRol.isEmpty()) {
            throw new IllegalStateException("El rol tiene usuarios asociados");
        }

        rolUsuarioRepository.deleteById(id);
    }

    // ═══ DIRECCIONES ══════════════════════════════════════════════════

    public List<Direccion> listarDireccionesPorUsuario(Long usuarioId) {
        return direccionRepository.findByUsuarioId(usuarioId);
    }

    @Transactional
    public Direccion agregarDireccion(Long usuarioId, Direccion direccion) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + usuarioId));

        direccion.setUsuario(usuario);

        return direccionRepository.save(direccion);
    }

    @Transactional
    public void eliminarDireccion(Long id) {

        if (!direccionRepository.existsById(id)) {
            throw new RuntimeException("Dirección no encontrada: " + id);
        }

        direccionRepository.deleteById(id);
    }

    // ═══ OPERACIONES ESPECÍFICAS ═══════════════════════════════════════

    @Transactional
    public Usuario cambiarActivo(Long id, Boolean activo) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + id));

        usuario.setActivo(activo);

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario actualizarRol(Long usuarioId, Long rolId) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + usuarioId));

        RolUsuario rol = rolUsuarioRepository.findById(rolId)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + rolId));

        usuario.setRol(rol);

        return usuarioRepository.save(usuario);
    }
}