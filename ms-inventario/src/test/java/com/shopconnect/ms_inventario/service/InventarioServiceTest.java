package com.shopconnect.ms_inventario.service;

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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.shopconnect.ms_inventario.dto.request.InventarioRequestDTO;
import com.shopconnect.ms_inventario.dto.request.MovimientoInventarioRequestDTO;
import com.shopconnect.ms_inventario.dto.response.InventarioResponseDTO;
import com.shopconnect.ms_inventario.dto.response.MovimientoInventarioResponseDTO;
import com.shopconnect.ms_inventario.model.Inventario;
import com.shopconnect.ms_inventario.model.MovimientoInventario;
import com.shopconnect.ms_inventario.repository.InventarioRepository;
import com.shopconnect.ms_inventario.repository.MovimientoInventarioRepository;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private MovimientoInventarioRepository movimientoInventarioRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private InventarioService inventarioService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(inventarioService, "productosUrl", "http://localhost:8082");
    }

    @Test
    void crearInventario_exitosamente() {
        InventarioRequestDTO dto = new InventarioRequestDTO();
        dto.setProductoId(5L);
        dto.setStockActual(20);
        dto.setStockMinimo(3);

        Inventario guardado = crearInventario(10L, 5L, 20, 3);

        when(restTemplate.getForObject(anyString(), eq(Object.class))).thenReturn(new Object());
        when(inventarioRepository.existsByProductoId(5L)).thenReturn(false);
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(guardado);

        InventarioResponseDTO resultado = inventarioService.crear(dto);

        assertNotNull(resultado);
        assertEquals(10L, resultado.getId());
        assertEquals(5L, resultado.getProductoId());
        assertEquals(20, resultado.getStockActual());
        assertEquals(3, resultado.getStockMinimo());
        verify(inventarioRepository).save(any(Inventario.class));
    }

    @Test
    void crearInventario_lanzaExcepcion_siProductoNoExiste() {
        InventarioRequestDTO dto = new InventarioRequestDTO();
        dto.setProductoId(99L);

        when(restTemplate.getForObject(anyString(), eq(Object.class))).thenThrow(new RuntimeException("No encontrado"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> inventarioService.crear(dto));

        assertEquals("Producto no encontrado: 99", ex.getMessage());
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void registrarMovimientoEntrada_aumentaStockYGuardaMovimiento() {
        Inventario inventario = crearInventario(10L, 5L, 20, 3);

        MovimientoInventarioRequestDTO dto = new MovimientoInventarioRequestDTO();
        dto.setTipo("entrada");
        dto.setCantidad(5);

        MovimientoInventario guardado = new MovimientoInventario();
        guardado.setId(100L);
        guardado.setTipo("ENTRADA");
        guardado.setCantidad(5);
        guardado.setInventario(inventario);

        when(inventarioRepository.findById(10L)).thenReturn(Optional.of(inventario));
        when(movimientoInventarioRepository.save(any(MovimientoInventario.class))).thenReturn(guardado);

        MovimientoInventarioResponseDTO resultado = inventarioService.registrarMovimiento(10L, dto);

        assertEquals(100L, resultado.getId());
        assertEquals("ENTRADA", resultado.getTipo());
        assertEquals(25, inventario.getStockActual());
        verify(inventarioRepository).save(inventario);
        verify(movimientoInventarioRepository).save(any(MovimientoInventario.class));
    }

    @Test
    void registrarMovimientoSalida_lanzaExcepcion_siStockInsuficiente() {
        Inventario inventario = crearInventario(10L, 5L, 2, 1);

        MovimientoInventarioRequestDTO dto = new MovimientoInventarioRequestDTO();
        dto.setTipo("SALIDA");
        dto.setCantidad(5);

        when(inventarioRepository.findById(10L)).thenReturn(Optional.of(inventario));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> inventarioService.registrarMovimiento(10L, dto));

        assertEquals("Stock insuficiente para realizar la salida", ex.getMessage());
        verify(movimientoInventarioRepository, never()).save(any(MovimientoInventario.class));
    }

    @Test
    void listar_devuelveInventarios() {
        Inventario inventario = crearInventario(10L, 5L, 20, 3);
        when(inventarioRepository.findAll()).thenReturn(List.of(inventario));

        List<InventarioResponseDTO> resultado = inventarioService.listar();

        assertEquals(1, resultado.size());
        assertEquals(10L, resultado.get(0).getId());
        verify(inventarioRepository).findAll();
    }

    private Inventario crearInventario(Long id, Long productoId, Integer stockActual, Integer stockMinimo) {
        Inventario inventario = new Inventario();
        inventario.setId(id);
        inventario.setProductoId(productoId);
        inventario.setStockActual(stockActual);
        inventario.setStockMinimo(stockMinimo);
        return inventario;
    }
}
