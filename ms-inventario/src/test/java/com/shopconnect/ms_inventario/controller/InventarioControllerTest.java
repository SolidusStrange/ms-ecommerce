package com.shopconnect.ms_inventario.controller;

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

import com.shopconnect.ms_inventario.dto.request.InventarioRequestDTO;
import com.shopconnect.ms_inventario.dto.request.MovimientoInventarioRequestDTO;
import com.shopconnect.ms_inventario.dto.response.InventarioResponseDTO;
import com.shopconnect.ms_inventario.dto.response.MovimientoInventarioResponseDTO;
import com.shopconnect.ms_inventario.service.InventarioService;

@ExtendWith(MockitoExtension.class)
class InventarioControllerTest {

    @Mock
    private InventarioService inventarioService;

    @InjectMocks
    private InventarioController inventarioController;

    @Test
    void listarTodos_devuelveInventarios() {
        InventarioResponseDTO inventario = crearInventarioResponse();
        when(inventarioService.listar()).thenReturn(List.of(inventario));

        ResponseEntity<?> respuesta = inventarioController.listarTodos(null);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(1, ((List<?>) respuesta.getBody()).size());
        verify(inventarioService).listar();
    }

    @Test
    void listarTodos_filtraPorProductoId() {
        InventarioResponseDTO inventario = crearInventarioResponse();
        when(inventarioService.buscarPorProductoId(5L)).thenReturn(inventario);

        ResponseEntity<?> respuesta = inventarioController.listarTodos(5L);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(inventario, respuesta.getBody());
        verify(inventarioService).buscarPorProductoId(5L);
        verify(inventarioService, never()).listar();
    }

    @Test
    void buscarPorId_devuelveNotFound_siNoExiste() {
        when(inventarioService.buscarPorId(99L)).thenThrow(new RuntimeException("Inventario no encontrado"));

        ResponseEntity<?> respuesta = inventarioController.buscarPorId(99L);

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertNull(respuesta.getBody());
        verify(inventarioService).buscarPorId(99L);
    }

    @Test
    void crear_devuelveCreated_siServiceCreaInventario() {
        InventarioRequestDTO request = new InventarioRequestDTO();
        InventarioResponseDTO creado = crearInventarioResponse();
        when(inventarioService.crear(request)).thenReturn(creado);

        ResponseEntity<?> respuesta = inventarioController.crear(request);

        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertSame(creado, respuesta.getBody());
        verify(inventarioService).crear(request);
    }

    @Test
    void crear_devuelveConflict_siServiceLanzaRuntimeException() {
        InventarioRequestDTO request = new InventarioRequestDTO();
        when(inventarioService.crear(request)).thenThrow(new RuntimeException("Ya existe inventario para el productoId: 5"));

        ResponseEntity<?> respuesta = inventarioController.crear(request);

        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
        assertTrue(respuesta.getBody().toString().contains("Ya existe inventario"));
        verify(inventarioService).crear(request);
    }

    @Test
    void ajustarStockMinimo_devuelveBadRequest_siNoVieneCampo() {
        ResponseEntity<?> respuesta = inventarioController.ajustarStockMinimo(10L, Map.of());

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertTrue(respuesta.getBody().toString().contains("stockMinimo"));
        verify(inventarioService, never()).ajustarStockMinimo(10L, null);
    }

    @Test
    void registrarMovimiento_devuelveConflict_siStockInsuficiente() {
        MovimientoInventarioRequestDTO request = new MovimientoInventarioRequestDTO();
        when(inventarioService.registrarMovimiento(10L, request))
                .thenThrow(new RuntimeException("Stock insuficiente para realizar la salida"));

        ResponseEntity<?> respuesta = inventarioController.registrarMovimiento(10L, request);

        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
        assertTrue(respuesta.getBody().toString().contains("insuficiente"));
        verify(inventarioService).registrarMovimiento(10L, request);
    }

    @Test
    void listarMovimientosPorInventario_devuelveMovimientos() {
        MovimientoInventarioResponseDTO movimiento = new MovimientoInventarioResponseDTO();
        movimiento.setId(100L);
        when(inventarioService.listarMovimientosPorInventario(10L)).thenReturn(List.of(movimiento));

        ResponseEntity<?> respuesta = inventarioController.listarMovimientosPorInventario(10L);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(1, ((List<?>) respuesta.getBody()).size());
        verify(inventarioService).listarMovimientosPorInventario(10L);
    }

    private InventarioResponseDTO crearInventarioResponse() {
        InventarioResponseDTO inventario = new InventarioResponseDTO();
        inventario.setId(10L);
        inventario.setProductoId(5L);
        inventario.setStockActual(20);
        inventario.setStockMinimo(3);
        return inventario;
    }
}
