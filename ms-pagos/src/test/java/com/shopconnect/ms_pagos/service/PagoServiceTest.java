package com.shopconnect.ms_pagos.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.shopconnect.ms_pagos.dto.request.PagoRequestDTO;
import com.shopconnect.ms_pagos.dto.request.TransaccionPagoRequestDTO;
import com.shopconnect.ms_pagos.dto.response.PagoResponseDTO;
import com.shopconnect.ms_pagos.dto.response.TransaccionPagoResponseDTO;
import com.shopconnect.ms_pagos.model.MetodoPago;
import com.shopconnect.ms_pagos.model.Pago;
import com.shopconnect.ms_pagos.model.TransaccionPago;
import com.shopconnect.ms_pagos.repository.MetodoPagoRepository;
import com.shopconnect.ms_pagos.repository.PagoRepository;
import com.shopconnect.ms_pagos.repository.TransaccionPagoRepository;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private MetodoPagoRepository metodoPagoRepository;

    @Mock
    private TransaccionPagoRepository transaccionPagoRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PagoService pagoService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(pagoService, "pedidosBaseUrl", "http://localhost:8083/api/v1/pedidos");
    }

    @Test
    void procesarPago_exitosamente() {
        PagoRequestDTO dto = new PagoRequestDTO();
        dto.setPedidoId(10L);
        dto.setMetodoPagoId(1L);
        dto.setMonto(25000.0);

        MetodoPago metodoPago = crearMetodoPago(1L, true);
        Pago guardado = crearPago(100L, 10L, metodoPago, "PENDIENTE");

        when(restTemplate.getForEntity(anyString(), eq(Object.class))).thenReturn(ResponseEntity.ok(new Object()));
        when(metodoPagoRepository.findById(1L)).thenReturn(Optional.of(metodoPago));
        when(pagoRepository.save(any(Pago.class))).thenReturn(guardado);
        when(transaccionPagoRepository.save(any(TransaccionPago.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PagoResponseDTO resultado = pagoService.procesar(dto);

        assertNotNull(resultado);
        assertEquals(100L, resultado.getId());
        assertEquals(10L, resultado.getPedidoId());
        assertEquals(25000.0, resultado.getMonto());
        assertEquals("PENDIENTE", resultado.getEstado());
        assertEquals(1L, resultado.getMetodoPagoId());
        verify(transaccionPagoRepository).save(any(TransaccionPago.class));
    }

    @Test
    void procesarPago_lanzaExcepcion_siPedidoNoExiste() {
        PagoRequestDTO dto = new PagoRequestDTO();
        dto.setPedidoId(99L);
        dto.setMetodoPagoId(1L);

        when(restTemplate.getForEntity(anyString(), eq(Object.class)))
                .thenThrow(new RestClientException("No encontrado"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> pagoService.procesar(dto));

        assertEquals("Pedido no encontrado: 99", ex.getMessage());
        verify(metodoPagoRepository, never()).findById(any());
        verify(pagoRepository, never()).save(any(Pago.class));
    }

    @Test
    void actualizarEstado_actualizaPagoExitosamente() {
        Pago pago = crearPago(100L, 10L, crearMetodoPago(1L, true), "PENDIENTE");

        when(pagoRepository.findById(100L)).thenReturn(Optional.of(pago));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PagoResponseDTO resultado = pagoService.actualizarEstado(100L, "pagado");

        assertEquals(100L, resultado.getId());
        assertEquals("PAGADO", resultado.getEstado());
        verify(pagoRepository).save(pago);
    }

    @Test
    void agregarTransaccion_guardaTransaccionParaPago() {
        Pago pago = crearPago(100L, 10L, crearMetodoPago(1L, true), "PENDIENTE");

        TransaccionPagoRequestDTO dto = new TransaccionPagoRequestDTO();
        dto.setCodigoTransaccion("TX-123");
        dto.setEstado("aprobada");

        TransaccionPago transaccion = new TransaccionPago();
        transaccion.setId(200L);
        transaccion.setCodigoTransaccion("TX-123");
        transaccion.setEstado("APROBADA");
        transaccion.setPago(pago);

        when(pagoRepository.findById(100L)).thenReturn(Optional.of(pago));
        when(transaccionPagoRepository.save(any(TransaccionPago.class))).thenReturn(transaccion);

        TransaccionPagoResponseDTO resultado = pagoService.agregarTransaccion(100L, dto);

        assertEquals(200L, resultado.getId());
        assertEquals("TX-123", resultado.getCodigoTransaccion());
        assertEquals("APROBADA", resultado.getEstado());
        assertEquals(100L, resultado.getPagoId());
        verify(transaccionPagoRepository).save(any(TransaccionPago.class));
    }

    @Test
    void listarTodos_devuelvePagos() {
        Pago pago = crearPago(100L, 10L, crearMetodoPago(1L, true), "PENDIENTE");
        when(pagoRepository.findAll()).thenReturn(List.of(pago));

        List<PagoResponseDTO> resultado = pagoService.listarTodos();

        assertEquals(1, resultado.size());
        assertEquals(100L, resultado.get(0).getId());
        verify(pagoRepository).findAll();
    }

    private Pago crearPago(Long id, Long pedidoId, MetodoPago metodoPago, String estado) {
        Pago pago = new Pago();
        pago.setId(id);
        pago.setPedidoId(pedidoId);
        pago.setMonto(25000.0);
        pago.setEstado(estado);
        pago.setMetodoPago(metodoPago);
        return pago;
    }

    private MetodoPago crearMetodoPago(Long id, Boolean activo) {
        MetodoPago metodoPago = new MetodoPago();
        metodoPago.setId(id);
        metodoPago.setNombre("Tarjeta");
        metodoPago.setActivo(activo);
        return metodoPago;
    }

}
