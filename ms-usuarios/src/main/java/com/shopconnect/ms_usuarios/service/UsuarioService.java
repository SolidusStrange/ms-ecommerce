package com.shopconnect.ms_usuarios.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopconnect.ms_usuarios.dto.DireccionDTO;
import com.shopconnect.ms_usuarios.dto.RolUsuarioDTO;
import com.shopconnect.ms_usuarios.dto.UsuarioDTO;
import com.shopconnect.ms_usuarios.dto.request.DireccionRequestDTO;
import com.shopconnect.ms_usuarios.dto.request.RolUsuarioRequestDTO;
import com.shopconnect.ms_usuarios.dto.request.UsuarioRequestDTO;
import com.shopconnect.ms_usuarios.model.Direccion;
import com.shopconnect.ms_usuarios.model.RolUsuario;
import com.shopconnect.ms_usuarios.model.Usuario;
import com.shopconnect.ms_usuarios.repository.DireccionRepository;
import com.shopconnect.ms_usuarios.repository.RolUsuarioRepository;
import com.shopconnect.ms_usuarios.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolUsuarioRepository rolUsuarioRepository;
    private final DireccionRepository direccionRepository;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          RolUsuarioRepository rolUsuarioRepository,
                          DireccionRepository direccionRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolUsuarioRepository = rolUsuarioRepository;
        this.direccionRepository = direccionRepository;
    }

    public List<UsuarioDTO> listarTodos() {
        return usuarioRepository.findAll().stream().map(this::convertirADTO).toList();
    }

    public UsuarioDTO buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return convertirADTO(usuario);
    }

    public UsuarioDTO buscarPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return convertirADTO(usuario);
    }

    public List<UsuarioDTO> listarPorRol(Long rolId) {
        return usuarioRepository.findByRolId(rolId).stream().map(this::convertirADTO).toList();
    }

    public List<UsuarioDTO> listarPorActivo(Boolean activo) {
        return usuarioRepository.findByActivo(activo).stream().map(this::convertirADTO).toList();
    }

    @Transactional
    public UsuarioDTO crear(UsuarioRequestDTO dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email duplicado: " + dto.getEmail());
        }

        RolUsuario rol = rolUsuarioRepository.findById(dto.getRolId())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + dto.getRolId()));

        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(dto.getPassword());
        usuario.setActivo(dto.getActivo());
        usuario.setRol(rol);

        return convertirADTO(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioDTO actualizar(Long id, UsuarioRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!dto.getEmail().equals(usuario.getEmail()) && usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email duplicado: " + dto.getEmail());
        }

        RolUsuario rol = rolUsuarioRepository.findById(dto.getRolId())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + dto.getRolId()));

        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(dto.getPassword());
        usuario.setActivo(dto.getActivo());
        usuario.setRol(rol);

        return convertirADTO(usuarioRepository.save(usuario));
    }

    @Transactional
    public void eliminar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuarioRepository.delete(usuario);
    }

    public List<RolUsuarioDTO> listarRoles() {
        return rolUsuarioRepository.findAll().stream().map(this::convertirRolADTO).toList();
    }

    public RolUsuarioDTO buscarRolPorId(Long id) {
        RolUsuario rol = rolUsuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        return convertirRolADTO(rol);
    }

    @Transactional
    public RolUsuarioDTO crearRol(RolUsuarioRequestDTO dto) {
        if (rolUsuarioRepository.existsByNombre(dto.getNombre())) {
            throw new IllegalArgumentException("Nombre de rol duplicado: " + dto.getNombre());
        }

        RolUsuario rol = new RolUsuario();
        rol.setNombre(dto.getNombre());
        rol.setDescripcion(dto.getDescripcion());

        return convertirRolADTO(rolUsuarioRepository.save(rol));
    }

    @Transactional
    public void eliminarRol(Long id) {
        RolUsuario rol = rolUsuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        List<Usuario> usuariosConRol = usuarioRepository.findByRolId(id);
        if (!usuariosConRol.isEmpty()) {
            throw new IllegalStateException("El rol tiene usuarios asociados");
        }

        rolUsuarioRepository.delete(rol);
    }

    public List<DireccionDTO> listarDireccionesPorUsuario(Long usuarioId) {
        return direccionRepository.findByUsuarioId(usuarioId).stream().map(this::convertirDireccionADTO).toList();
    }

    @Transactional
    public DireccionDTO agregarDireccion(Long usuarioId, DireccionRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Direccion direccion = new Direccion();
        direccion.setCalle(dto.getCalle());
        direccion.setCiudad(dto.getCiudad());
        direccion.setRegion(dto.getRegion());
        direccion.setUsuario(usuario);

        return convertirDireccionADTO(direccionRepository.save(direccion));
    }

    @Transactional
    public void eliminarDireccion(Long id) {
        Direccion direccion = direccionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dirección no encontrada"));
        direccionRepository.delete(direccion);
    }

    @Transactional
    public UsuarioDTO cambiarActivo(Long id, Boolean activo) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setActivo(activo);
        return convertirADTO(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioDTO actualizarRol(Long usuarioId, Long rolId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        RolUsuario rol = rolUsuarioRepository.findById(rolId)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        usuario.setRol(rol);
        return convertirADTO(usuarioRepository.save(usuario));
    }

    private UsuarioDTO convertirADTO(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.getId());
        dto.setNombre(usuario.getNombre());
        dto.setEmail(usuario.getEmail());
        dto.setActivo(usuario.getActivo());
        dto.setRolId(usuario.getRol() != null ? usuario.getRol().getId() : null);
        return dto;
    }

    private RolUsuarioDTO convertirRolADTO(RolUsuario rol) {
        RolUsuarioDTO dto = new RolUsuarioDTO();
        dto.setId(rol.getId());
        dto.setNombre(rol.getNombre());
        dto.setDescripcion(rol.getDescripcion());
        return dto;
    }

    private DireccionDTO convertirDireccionADTO(Direccion direccion) {
        DireccionDTO dto = new DireccionDTO();
        dto.setId(direccion.getId());
        dto.setCalle(direccion.getCalle());
        dto.setCiudad(direccion.getCiudad());
        dto.setRegion(direccion.getRegion());
        dto.setUsuarioId(direccion.getUsuario() != null ? direccion.getUsuario().getId() : null);
        return dto;
    }
}