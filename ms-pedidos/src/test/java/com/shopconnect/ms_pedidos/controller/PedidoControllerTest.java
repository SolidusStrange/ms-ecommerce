package com.shopconnect.ms_pedidos.controller;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.shopconnect.ms_pedidos.dto.request.PedidoRequestDTO;
import com.shopconnect.ms_pedidos.dto.response.PedidoResponseDTO;
import com.shopconnect.ms_pedidos.service.PedidoService;

@ExtendWith(MockitoExtension.class)
class PedidoControllerTest {
   
    @Mock
    private PedidoService pedidoService;

    @InjectMocks
    private PedidoController pedidoController;
    
    @Test
    void listarTodos_devuelvePedidos() {
        PedidoResponseDTO pedido = crearPedidoResponse();
        when(pedidoService.listarTodos()).thenReturn(List.of(pedido));

        ResponseEntity<List<PedidoResponseDTO>> respuesta = pedidoController.listarTodos(null, null);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(1, respuesta.getBody().size());
        assertEquals(10L, respuesta.getBody().get(0).getId());
        verify(pedidoService).listarTodos();
    }

    @Test
    void listarTodos_filtraPorUsuario() {
        PedidoResponseDTO pedido = crearPedidoResponse();
        when(pedidoService.listarPorUsuario(1L)).thenReturn(List.of(pedido));

        ResponseEntity<List<PedidoResponseDTO>> respuesta = pedidoController.listarTodos(1L, null);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(1L, respuesta.getBody().get(0).getUsuarioId());
        verify(pedidoService).listarPorUsuario(1L);
        verify(pedidoService, never()).listarTodos();
        verify(pedidoService, never()).listarPorEstado(anyLong());
        
    }

    @Test
    void buscarPorId_devuelveOk_siExiste() {
        PedidoResponseDTO pedido = crearPedidoResponse();
        when(pedidoService.buscarPorId(10L)).thenReturn(Optional.of(pedido));

        ResponseEntity<PedidoResponseDTO> respuesta = pedidoController.buscarPorId(10L);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(10L, respuesta.getBody().getId());

        verify(pedidoService).buscarPorId(10L);
    }

    @Test
    void buscarPorId_devuelveNotFound_siNoExiste() {
        when(pedidoService.buscarPorId(99L)).thenReturn(Optional.empty());

        ResponseEntity<PedidoResponseDTO> respuesta = pedidoController.buscarPorId(99L);

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertNull(respuesta.getBody());

        verify(pedidoService).buscarPorId(99L);
    }

    @Test
    void crear_devuelveCreated_siServiceCreaPedido() {
        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setUsuarioId(1L);
        request.setEstadoId(1L);
        request.setTotal(25000.0);

        PedidoResponseDTO creado = crearPedidoResponse();
        when(pedidoService.crear(request)).thenReturn(creado);

        ResponseEntity<?> respuesta = pedidoController.crear(request);

        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertSame(creado, respuesta.getBody());

        verify(pedidoService).crear(request);
    }

    @Test
    void crear_devuelveBadRequest_siServiceLanzaRuntimeException() {
        PedidoRequestDTO request = new PedidoRequestDTO();
        when(pedidoService.crear(request)).thenThrow(new RuntimeException("Usuario no encontrado: 99"));

        ResponseEntity<?> respuesta = pedidoController.crear(request);

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertTrue(respuesta.getBody().toString().contains("Usuario no encontrado: 99"));

        verify(pedidoService).crear(request);
    }

    @Test
    void eliminar_devuelveNoContent_siElimina() {
        ResponseEntity<?> respuesta = pedidoController.eliminar(10L);

        assertEquals(HttpStatus.NO_CONTENT, respuesta.getStatusCode());
        verify(pedidoService).eliminar(10L);
    }

    @Test
    void eliminar_devuelveNotFound_siServiceLanzaRuntimeException() {
        doThrow(new RuntimeException("Pedido no encontrado: 99")).when(pedidoService).eliminar(99L);

        ResponseEntity<?> respuesta = pedidoController.eliminar(99L);

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());

        verify(pedidoService).eliminar(99L);

        
    }

    private PedidoResponseDTO crearPedidoResponse() {
        PedidoResponseDTO pedido = new PedidoResponseDTO();
        pedido.setId(10L);
        pedido.setUsuarioId(1L);
        pedido.setEstadoId(1L);
        pedido.setTotal(25000.0);
        return pedido;
    }

}