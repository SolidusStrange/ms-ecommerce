package com.shopconnect.ms_pedidos.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.shopconnect.ms_pedidos.dto.request.DetallePedidoRequestDTO;
import com.shopconnect.ms_pedidos.dto.request.PedidoRequestDTO;
import com.shopconnect.ms_pedidos.dto.response.DetallePedidoResponseDTO;
import com.shopconnect.ms_pedidos.dto.response.PedidoResponseDTO;
import com.shopconnect.ms_pedidos.model.DetallePedido;
import com.shopconnect.ms_pedidos.model.EstadoPedido;
import com.shopconnect.ms_pedidos.model.Pedido;
import com.shopconnect.ms_pedidos.repository.DetallePedidoRepository;
import com.shopconnect.ms_pedidos.repository.EstadoPedidoRepository;
import com.shopconnect.ms_pedidos.repository.PedidoRepository;

// le decimos a Junit que use Mockito dentro de esta clase test. Mockito inicializa los mocks automáticametne antes de ejecutar los métodos de prueba
@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    // creamos un objeto falso, simulado. No es el repositorio real de la bd. Sirve para controlar qué devuelve en cada test, sin tocar la BD real.    
    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private EstadoPedidoRepository estadoPedidoRepository;

    @Mock
    private DetallePedidoRepository detallePedidoRepository;

    @Mock
    private RestTemplate restTemplate;

    // Mockito crea una instancia real de PedidoService e inyecta dentro de ella los mocks que declaramos arriba    
    @InjectMocks
    private PedidoService pedidoService;

    // Metodo que se ejecuta antes de cada test. Deja el entorno listo.
    @BeforeEach
    void setUp() {
        // usamos reflectiontestutils porque como es una prueba unitaria no levantamos spring, por lo que
        // las variables quedarían null, así que con setfield le asignamos un valor manualmente para que RestTemplate pueda
        // construir la URL y la prueba funcione

        ReflectionTestUtils.setField(
                pedidoService,
                "usuariosBaseUrl",
                "http://localhost:8081/api/v1/usuarios");

        ReflectionTestUtils.setField(
                pedidoService,
                "productosBaseUrl",
                "http://localhost:8082/api/v1/productos");
    }


    // Testiaremos el flujo del microservicio
    @Test
    void crearPedido_exitosamente() {

        // Creamos DTO de entrada (simulando el JSON que enviaria el cliente)
        PedidoRequestDTO dto = new PedidoRequestDTO();
        dto.setUsuarioId(1L);
        dto.setEstadoId(1L);
        dto.setTotal(25000.0);

        // Preparamos datos simulados de salida
        EstadoPedido estado = new EstadoPedido();
        estado.setId(1L);
        estado.setNombre("Pendiente");

        Pedido pedidoGuardado = new Pedido();
        pedidoGuardado.setId(10L);
        pedidoGuardado.setUsuarioId(1L);
        pedidoGuardado.setEstado(estado);
        pedidoGuardado.setTotal(25000.0);

        // Cuando se llame a getForEntity con cualquier URL devolvemos una repsuesta HTTP 200 con un objeto cualquiera
        when(restTemplate.getForEntity(
                anyString(),
                eq(Object.class)))
                .thenReturn(ResponseEntity.ok(new Object()));

        // Si el servicio pide el estado con ID 1, el mock responde si existe y devuelve el estado
        when(estadoPedidoRepository.findById(1L))
                .thenReturn(Optional.of(estado));

        // Si se intenta guardar cualquier pedido, devuelve el objeto pedidoGuardado
        when(pedidoRepository.save(any(Pedido.class)))
                .thenReturn(pedidoGuardado);

        // Aca probamos el comportamiento real del servicio
        PedidoResponseDTO resultado = pedidoService.crear(dto);

        // Verificamos que el resultado no sea nulo y que tenga los valores esperados. Si algo no coincide, el test falla.
        assertNotNull(resultado);
        assertEquals(10L, resultado.getId());
        assertEquals(1L, resultado.getUsuarioId());
        assertEquals(25000.0, resultado.getTotal());
        assertEquals(1L, resultado.getEstadoId());

        // Esto comprueba que el metodo fue llamado exactamente una vez. Solo verifica interacciones con mocks      
        verify(restTemplate, times(1))
                .getForEntity(anyString(), eq(Object.class));

        verify(estadoPedidoRepository, times(1))
                .findById(1L);

        verify(pedidoRepository, times(1))
                .save(any(Pedido.class));
    }

    
    // comprobamos si falla la consulta al microservicios usuarios
    @Test
    void crearPedido_lanzaExcepcion_siUsuarioNoExiste() {

        PedidoRequestDTO dto = new PedidoRequestDTO();
        dto.setUsuarioId(99L);
        dto.setEstadoId(1L);
        dto.setTotal(25000.0);

        // significa que la llamada simulada a usuarios lanzara una excepcion
        when(restTemplate.getForEntity(anyString(), eq(Object.class)))
                .thenThrow(new RestClientException("No encontrado"));

        // Verifica que el bloque lance una excepcion del tipo DTO
        RuntimeException ex = assertThrows(RuntimeException.class, () -> pedidoService.crear(dto));

        assertEquals("Usuario no encontrado: 99", ex.getMessage());
        verify(estadoPedidoRepository, never()).findById(anyLong());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    // Probamos algo similar pero ahora si el usuario existiese y el problema estuviera en estado
    @Test
    void crearPedido_lanzaExcepcion_siEstadoNoExiste() {

        PedidoRequestDTO dto = new PedidoRequestDTO();
        dto.setUsuarioId(1L);
        dto.setEstadoId(99L);

        when(restTemplate.getForEntity(anyString(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok(new Object()));

        when(estadoPedidoRepository.findById(99L))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> pedidoService.crear(dto));

        assertEquals("Estado no encontrado: 99", ex.getMessage());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    // buscamos un pedido existente, le cambiamos el estado y lo guardamos
    @Test
    void cambiarEstado_actualizaPedidoExitosamente() {

        EstadoPedido estadoActual = new EstadoPedido();
        estadoActual.setId(1L);
        estadoActual.setNombre("Pendiente");

        EstadoPedido estadoNuevo = new EstadoPedido();
        estadoNuevo.setId(2L);
        estadoNuevo.setNombre("Pagado");

        Pedido pedido = new Pedido();
        pedido.setId(10L);
        pedido.setUsuarioId(1L);
        pedido.setEstado(estadoActual);
        pedido.setTotal(25000.0);

        when(pedidoRepository.findById(10L)).thenReturn(Optional.of(pedido));
        when(estadoPedidoRepository.findById(2L)).thenReturn(Optional.of(estadoNuevo));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PedidoResponseDTO resultado = pedidoService.cambiarEstado(10L, 2L);

        assertEquals(10L, resultado.getId());
        assertEquals(2L, resultado.getEstadoId());
        assertEquals("Pagado", pedido.getEstado().getNombre());
        verify(pedidoRepository).save(pedido);
    }


    // hacemos una prueba de agregar un detalle a un pedido y recalculamos el total
    @Test
    void agregarDetalle_guardaDetalleYRecalculaTotal() {

        Pedido pedido = new Pedido();
        pedido.setId(10L);
        pedido.setUsuarioId(1L);
        pedido.setTotal(0.0);

        DetallePedidoRequestDTO dto = new DetallePedidoRequestDTO();
        dto.setProductoId(5L);
        dto.setCantidad(2);
        dto.setPrecioUnit(1500.0);

        DetallePedido detalleGuardado = new DetallePedido();
        detalleGuardado.setId(100L);
        detalleGuardado.setPedido(pedido);
        detalleGuardado.setProductoId(5L);
        detalleGuardado.setCantidad(2);
        detalleGuardado.setPrecioUnit(1500.0);

        when(pedidoRepository.findById(10L)).thenReturn(Optional.of(pedido));
        when(restTemplate.getForEntity(anyString(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok(new Object()));
        when(detallePedidoRepository.save(any(DetallePedido.class))).thenReturn(detalleGuardado);
        when(detallePedidoRepository.findByPedidoId(10L)).thenReturn(List.of(detalleGuardado));

        DetallePedidoResponseDTO resultado = pedidoService.agregarDetalle(10L, dto);

        assertEquals(100L, resultado.getId());
        assertEquals(5L, resultado.getProductoId());
        assertEquals(10L, resultado.getPedidoId());
        assertEquals(3000.0, pedido.getTotal());

        verify(detallePedidoRepository)
        .findByPedidoId(10L);
        verify(detallePedidoRepository).save(any(DetallePedido.class));
        verify(pedidoRepository).save(pedido);
    }


}
