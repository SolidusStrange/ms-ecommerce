package com.shopconnect.ms_pagos.controller;

import java.util.List;
import java.util.Optional;

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

import com.shopconnect.ms_pagos.dto.request.PagoRequestDTO;
import com.shopconnect.ms_pagos.dto.response.PagoResponseDTO;
import com.shopconnect.ms_pagos.service.PagoService;

@ExtendWith(MockitoExtension.class)
class PagoControllerTest {

    @Mock
    private PagoService pagoService;

    @InjectMocks
    private PagoController pagoController;

    @Test
    void listarTodos_devuelvePagos() {
        PagoResponseDTO pago = crearPagoResponse();
        when(pagoService.listarTodos()).thenReturn(List.of(pago));

        ResponseEntity<List<PagoResponseDTO>> respuesta = pagoController.listarTodos(null, null);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(1, respuesta.getBody().size());
        verify(pagoService).listarTodos();
    }

    @Test
    void listarTodos_filtraPorPedido() {
        PagoResponseDTO pago = crearPagoResponse();
        when(pagoService.listarPorPedido(10L)).thenReturn(List.of(pago));

        ResponseEntity<List<PagoResponseDTO>> respuesta = pagoController.listarTodos(10L, null);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(10L, respuesta.getBody().get(0).getPedidoId());
        verify(pagoService).listarPorPedido(10L);
        verify(pagoService, never()).listarTodos();
    }

    @Test
    void buscarPorId_devuelveOk_siExiste() {
        PagoResponseDTO pago = crearPagoResponse();
        when(pagoService.buscarPorId(100L)).thenReturn(Optional.of(pago));

        ResponseEntity<PagoResponseDTO> respuesta = pagoController.buscarPorId(100L);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(100L, respuesta.getBody().getId());
        verify(pagoService).buscarPorId(100L);
    }

    @Test
    void buscarPorId_devuelveNotFound_siNoExiste() {
        when(pagoService.buscarPorId(99L)).thenReturn(Optional.empty());

        ResponseEntity<PagoResponseDTO> respuesta = pagoController.buscarPorId(99L);

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertNull(respuesta.getBody());
        verify(pagoService).buscarPorId(99L);
    }

    @Test
    void procesar_devuelveCreated_siServiceProcesaPago() {
        PagoRequestDTO request = new PagoRequestDTO();
        PagoResponseDTO creado = crearPagoResponse();
        when(pagoService.procesar(request)).thenReturn(creado);

        ResponseEntity<?> respuesta = pagoController.procesar(request);

        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertSame(creado, respuesta.getBody());
        verify(pagoService).procesar(request);
    }

    @Test
    void procesar_devuelveConflict_siMetodoPagoInactivo() {
        PagoRequestDTO request = new PagoRequestDTO();
        when(pagoService.procesar(request)).thenThrow(new IllegalStateException("El metodo de pago no esta activo"));

        ResponseEntity<?> respuesta = pagoController.procesar(request);

        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
        assertTrue(respuesta.getBody().toString().contains("metodo de pago"));
        verify(pagoService).procesar(request);
    }

    @Test
    void actualizarEstado_devuelveBadRequest_siEstadoVacio() {
        when(pagoService.actualizarEstado(100L, "")).thenThrow(new IllegalArgumentException("El estado es obligatorio"));

        ResponseEntity<?> respuesta = pagoController.actualizarEstado(100L, "");

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertTrue(respuesta.getBody().toString().contains("El estado es obligatorio"));
        verify(pagoService).actualizarEstado(100L, "");
    }

    @Test
    void eliminar_devuelveNotFound_siServiceLanzaRuntimeException() {
        doThrow(new RuntimeException("Pago no encontrado: 99")).when(pagoService).eliminar(99L);

        ResponseEntity<?> respuesta = pagoController.eliminar(99L);

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        verify(pagoService).eliminar(99L);
    }

    private PagoResponseDTO crearPagoResponse() {
        PagoResponseDTO pago = new PagoResponseDTO();
        pago.setId(100L);
        pago.setPedidoId(10L);
        pago.setMetodoPagoId(1L);
        pago.setMonto(25000.0);
        pago.setEstado("PENDIENTE");
        return pago;
    }
}
