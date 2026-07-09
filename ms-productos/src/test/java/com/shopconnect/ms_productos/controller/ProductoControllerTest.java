package com.shopconnect.ms_productos.controller;

import java.util.List;

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

import com.shopconnect.ms_productos.dto.request.ProductoRequestDTO;
import com.shopconnect.ms_productos.dto.response.ProductoResponseDTO;
import com.shopconnect.ms_productos.service.ProductoService;

@ExtendWith(MockitoExtension.class)
class ProductoControllerTest {

    @Mock
    private ProductoService productoService;

    @InjectMocks
    private ProductoController productoController;

    @Test
    void listarTodos_devuelveProductos() {
        ProductoResponseDTO producto = crearProductoResponse();
        when(productoService.listarTodos()).thenReturn(List.of(producto));

        ResponseEntity<List<ProductoResponseDTO>> respuesta = productoController.listarTodos(null, null, null);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(1, respuesta.getBody().size());
        assertEquals(10L, respuesta.getBody().get(0).getId());
        verify(productoService).listarTodos();
    }

    @Test
    void listarTodos_filtraPorNombre() {
        ProductoResponseDTO producto = crearProductoResponse();
        when(productoService.buscarPorNombre("note")).thenReturn(List.of(producto));

        ResponseEntity<List<ProductoResponseDTO>> respuesta = productoController.listarTodos("note", null, null);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals("Notebook", respuesta.getBody().get(0).getNombre());
        verify(productoService).buscarPorNombre("note");
        verify(productoService, never()).listarTodos();
    }

    @Test
    void buscarPorId_devuelveOk_siExiste() {
        ProductoResponseDTO producto = crearProductoResponse();
        when(productoService.buscarPorId(10L)).thenReturn(producto);

        ResponseEntity<?> respuesta = productoController.buscarPorId(10L);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(producto, respuesta.getBody());
        verify(productoService).buscarPorId(10L);
    }

    @Test
    void buscarPorId_devuelveNotFound_siNoExiste() {
        when(productoService.buscarPorId(99L)).thenThrow(new RuntimeException("Producto no encontrado: 99"));

        ResponseEntity<?> respuesta = productoController.buscarPorId(99L);

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertNull(respuesta.getBody());
        verify(productoService).buscarPorId(99L);
    }

    @Test
    void crear_devuelveCreated_siServiceCreaProducto() {
        ProductoRequestDTO request = new ProductoRequestDTO();
        ProductoResponseDTO creado = crearProductoResponse();
        when(productoService.crear(request)).thenReturn(creado);

        ResponseEntity<?> respuesta = productoController.crear(request);

        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertSame(creado, respuesta.getBody());
        verify(productoService).crear(request);
    }

    @Test
    void crear_devuelveConflict_siSkuDuplicado() {
        ProductoRequestDTO request = new ProductoRequestDTO();
        when(productoService.crear(request)).thenThrow(new IllegalArgumentException("SKU duplicado: NB-001"));

        ResponseEntity<?> respuesta = productoController.crear(request);

        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
        assertTrue(respuesta.getBody().toString().contains("SKU duplicado: NB-001"));
        verify(productoService).crear(request);
    }

    @Test
    void eliminar_devuelveNoContent_siElimina() {
        ResponseEntity<?> respuesta = productoController.eliminar(10L);

        assertEquals(HttpStatus.NO_CONTENT, respuesta.getStatusCode());
        verify(productoService).eliminar(10L);
    }

    @Test
    void eliminar_devuelveNotFound_siServiceLanzaRuntimeException() {
        doThrow(new RuntimeException("Producto no encontrado: 99")).when(productoService).eliminar(99L);

        ResponseEntity<?> respuesta = productoController.eliminar(99L);

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        verify(productoService).eliminar(99L);
    }

    private ProductoResponseDTO crearProductoResponse() {
        ProductoResponseDTO producto = new ProductoResponseDTO();
        producto.setId(10L);
        producto.setNombre("Notebook");
        producto.setPrecio(799990.0);
        producto.setStock(12);
        producto.setSku("NB-001");
        producto.setCategoriaId(1L);
        producto.setMarcaId(2L);
        return producto;
    }
}
