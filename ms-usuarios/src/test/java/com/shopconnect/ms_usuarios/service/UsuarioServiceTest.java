package com.shopconnect.ms_usuarios.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shopconnect.ms_usuarios.dto.DireccionDTO;
import com.shopconnect.ms_usuarios.dto.UsuarioDTO;
import com.shopconnect.ms_usuarios.dto.request.DireccionRequestDTO;
import com.shopconnect.ms_usuarios.dto.request.UsuarioRequestDTO;
import com.shopconnect.ms_usuarios.model.Direccion;
import com.shopconnect.ms_usuarios.model.RolUsuario;
import com.shopconnect.ms_usuarios.model.Usuario;
import com.shopconnect.ms_usuarios.repository.DireccionRepository;
import com.shopconnect.ms_usuarios.repository.RolUsuarioRepository;
import com.shopconnect.ms_usuarios.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolUsuarioRepository rolUsuarioRepository;

    @Mock
    private DireccionRepository direccionRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void crearUsuario_exitosamente() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setNombre("Ana Perez");
        dto.setEmail("ana@test.com");
        dto.setPassword("secret");
        dto.setActivo(true);
        dto.setRolId(1L);

        RolUsuario rol = crearRol(1L);
        Usuario guardado = crearUsuario(10L, rol);

        when(usuarioRepository.existsByEmail("ana@test.com")).thenReturn(false);
        when(rolUsuarioRepository.findById(1L)).thenReturn(Optional.of(rol));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(guardado);

        UsuarioDTO resultado = usuarioService.crear(dto);

        assertNotNull(resultado);
        assertEquals(10L, resultado.getId());
        assertEquals("Ana Perez", resultado.getNombre());
        assertEquals("ana@test.com", resultado.getEmail());
        assertEquals(true, resultado.getActivo());
        assertEquals(1L, resultado.getRolId());

        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void crearUsuario_lanzaExcepcion_siEmailDuplicado() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setEmail("ana@test.com");

        when(usuarioRepository.existsByEmail("ana@test.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> usuarioService.crear(dto));

        assertEquals("Email duplicado: ana@test.com", ex.getMessage());
        verify(rolUsuarioRepository, never()).findById(any());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void cambiarActivo_actualizaUsuarioExitosamente() {
        Usuario usuario = crearUsuario(10L, crearRol(1L));
        usuario.setActivo(true);

        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioDTO resultado = usuarioService.cambiarActivo(10L, false);

        assertEquals(10L, resultado.getId());
        assertEquals(false, resultado.getActivo());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void agregarDireccion_guardaDireccionParaUsuario() {
        Usuario usuario = crearUsuario(10L, crearRol(1L));

        DireccionRequestDTO dto = new DireccionRequestDTO();
        dto.setCalle("Av. Principal 123");
        dto.setCiudad("Santiago");
        dto.setRegion("RM");

        Direccion direccion = new Direccion();
        direccion.setId(100L);
        direccion.setCalle(dto.getCalle());
        direccion.setCiudad(dto.getCiudad());
        direccion.setRegion(dto.getRegion());
        direccion.setUsuario(usuario);

        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(direccionRepository.save(any(Direccion.class))).thenReturn(direccion);

        DireccionDTO resultado = usuarioService.agregarDireccion(10L, dto);

        assertEquals(100L, resultado.getId());
        assertEquals("Santiago", resultado.getCiudad());
        assertEquals(10L, resultado.getUsuarioId());
        verify(direccionRepository).save(any(Direccion.class));
    }

    @Test
    void listarTodos_devuelveUsuarios() {
        Usuario usuario = crearUsuario(10L, crearRol(1L));
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));

        List<UsuarioDTO> resultado = usuarioService.listarTodos();

        assertEquals(1, resultado.size());
        assertEquals(10L, resultado.get(0).getId());
        verify(usuarioRepository).findAll();
    }

    private Usuario crearUsuario(Long id, RolUsuario rol) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNombre("Ana Perez");
        usuario.setEmail("ana@test.com");
        usuario.setPassword("secret");
        usuario.setActivo(true);
        usuario.setRol(rol);
        return usuario;
    }

    private RolUsuario crearRol(Long id) {
        RolUsuario rol = new RolUsuario();
        rol.setId(id);
        rol.setNombre("CLIENTE");
        return rol;
    }
}
