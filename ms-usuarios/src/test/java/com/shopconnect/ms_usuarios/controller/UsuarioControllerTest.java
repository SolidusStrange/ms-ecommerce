package com.shopconnect.ms_usuarios.controller;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.shopconnect.ms_usuarios.dto.UsuarioDTO;
import com.shopconnect.ms_usuarios.dto.request.UsuarioRequestDTO;
import com.shopconnect.ms_usuarios.service.UsuarioService;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    @Test
    void listarTodos_devuelveUsuarios() {
        UsuarioDTO usuario = crearUsuarioResponse();
        when(usuarioService.listarTodos()).thenReturn(List.of(usuario));

        ResponseEntity<?> respuesta = usuarioController.listarTodos(null, null, null);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(1, ((List<?>) respuesta.getBody()).size());
        verify(usuarioService).listarTodos();
    }

    @Test
    void listarTodos_filtraPorEmail() {
        UsuarioDTO usuario = crearUsuarioResponse();
        when(usuarioService.buscarPorEmail("ana@test.com")).thenReturn(usuario);

        ResponseEntity<?> respuesta = usuarioController.listarTodos("ana@test.com", null, null);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(usuario, respuesta.getBody());
        verify(usuarioService).buscarPorEmail("ana@test.com");
        verify(usuarioService, never()).listarTodos();
    }

    @Test
    void buscarPorId_devuelveOk_siExiste() {
        UsuarioDTO usuario = crearUsuarioResponse();
        when(usuarioService.buscarPorId(10L)).thenReturn(usuario);

        ResponseEntity<?> respuesta = usuarioController.buscarPorId(10L);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(usuario, respuesta.getBody());
        verify(usuarioService).buscarPorId(10L);
    }

    @Test
    void buscarPorId_devuelveNotFound_siNoExiste() {
        when(usuarioService.buscarPorId(99L)).thenThrow(new RuntimeException("Usuario no encontrado"));

        ResponseEntity<?> respuesta = usuarioController.buscarPorId(99L);

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertNull(respuesta.getBody());
        verify(usuarioService).buscarPorId(99L);
    }

    @Test
    void crear_devuelveCreated_siServiceCreaUsuario() {
        UsuarioRequestDTO request = new UsuarioRequestDTO();
        UsuarioDTO creado = crearUsuarioResponse();
        when(usuarioService.crear(request)).thenReturn(creado);

        ResponseEntity<?> respuesta = usuarioController.crear(request);

        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertSame(creado, respuesta.getBody());
        verify(usuarioService).crear(request);
    }

    @Test
    void crear_devuelveConflict_siEmailDuplicado() {
        UsuarioRequestDTO request = new UsuarioRequestDTO();
        when(usuarioService.crear(request)).thenThrow(new IllegalArgumentException("Email duplicado: ana@test.com"));

        ResponseEntity<?> respuesta = usuarioController.crear(request);

        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
        assertTrue(respuesta.getBody().toString().contains("Email duplicado: ana@test.com"));
        verify(usuarioService).crear(request);
    }

    @Test
    void cambiarActivo_devuelveBadRequest_siNoVieneCampoActivo() {
        ResponseEntity<?> respuesta = usuarioController.cambiarActivo(10L, Map.of());

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertTrue(respuesta.getBody().toString().contains("activo"));
        verify(usuarioService, never()).cambiarActivo(10L, null);
    }

    @Test
    void eliminar_devuelveNotFound_siServiceLanzaRuntimeException() {
        doThrow(new RuntimeException("Usuario no encontrado")).when(usuarioService).eliminar(99L);

        ResponseEntity<?> respuesta = usuarioController.eliminar(99L);

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        verify(usuarioService).eliminar(99L);
    }

    private UsuarioDTO crearUsuarioResponse() {
        UsuarioDTO usuario = new UsuarioDTO();
        usuario.setId(10L);
        usuario.setNombre("Ana Perez");
        usuario.setEmail("ana@test.com");
        usuario.setActivo(true);
        usuario.setRolId(1L);
        return usuario;
    }
}
