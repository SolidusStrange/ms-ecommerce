package com.shopconnect.ms_productos.service;

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

import com.shopconnect.ms_productos.dto.request.ImagenProductoRequestDTO;
import com.shopconnect.ms_productos.dto.request.ProductoRequestDTO;
import com.shopconnect.ms_productos.dto.response.ImagenProductoResponseDTO;
import com.shopconnect.ms_productos.dto.response.ProductoResponseDTO;
import com.shopconnect.ms_productos.model.Categoria;
import com.shopconnect.ms_productos.model.ImagenProducto;
import com.shopconnect.ms_productos.model.Marca;
import com.shopconnect.ms_productos.model.Producto;
import com.shopconnect.ms_productos.repository.CategoriaRepository;
import com.shopconnect.ms_productos.repository.ImagenProductoRepository;
import com.shopconnect.ms_productos.repository.MarcaRepository;
import com.shopconnect.ms_productos.repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private MarcaRepository marcaRepository;

    @Mock
    private ImagenProductoRepository imagenProductoRepository;

    @InjectMocks
    private ProductoService productoService;

    @Test
    void crearProducto_exitosamente() {
        ProductoRequestDTO dto = new ProductoRequestDTO();
        dto.setNombre("Notebook");
        dto.setPrecio(799990.0);
        dto.setStock(12);
        dto.setSku("NB-001");
        dto.setCategoriaId(1L);
        dto.setMarcaId(2L);

        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Computacion");

        Marca marca = new Marca();
        marca.setId(2L);
        marca.setNombre("Lenovo");

        Producto guardado = new Producto();
        guardado.setId(10L);
        guardado.setNombre("Notebook");
        guardado.setPrecio(799990.0);
        guardado.setStock(12);
        guardado.setSku("NB-001");
        guardado.setCategoria(categoria);
        guardado.setMarca(marca);

        when(productoRepository.existsBySku("NB-001")).thenReturn(false);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(marcaRepository.findById(2L)).thenReturn(Optional.of(marca));
        when(productoRepository.save(any(Producto.class))).thenReturn(guardado);

        ProductoResponseDTO resultado = productoService.crear(dto);

        assertNotNull(resultado);
        assertEquals(10L, resultado.getId());
        assertEquals("Notebook", resultado.getNombre());
        assertEquals(799990.0, resultado.getPrecio());
        assertEquals(12, resultado.getStock());
        assertEquals("NB-001", resultado.getSku());
        assertEquals(1L, resultado.getCategoriaId());
        assertEquals(2L, resultado.getMarcaId());

        verify(productoRepository).save(any(Producto.class));
    }

    @Test
    void crearProducto_lanzaExcepcion_siSkuDuplicado() {
        ProductoRequestDTO dto = new ProductoRequestDTO();
        dto.setSku("NB-001");

        when(productoRepository.existsBySku("NB-001")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> productoService.crear(dto));

        assertEquals("SKU duplicado: NB-001", ex.getMessage());
        verify(categoriaRepository, never()).findById(any());
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void actualizarCategoria_actualizaProductoExitosamente() {
        Categoria categoriaActual = new Categoria();
        categoriaActual.setId(1L);

        Categoria categoriaNueva = new Categoria();
        categoriaNueva.setId(3L);
        categoriaNueva.setNombre("Accesorios");

        Producto producto = crearProducto(10L, categoriaActual, crearMarca(2L));

        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(categoriaRepository.findById(3L)).thenReturn(Optional.of(categoriaNueva));
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductoResponseDTO resultado = productoService.actualizarCategoria(10L, 3L);

        assertEquals(10L, resultado.getId());
        assertEquals(3L, resultado.getCategoriaId());
        verify(productoRepository).save(producto);
    }

    @Test
    void agregarImagen_guardaImagenParaProducto() {
        Producto producto = crearProducto(10L, crearCategoria(1L), crearMarca(2L));

        ImagenProductoRequestDTO dto = new ImagenProductoRequestDTO();
        dto.setUrl("https://img.test/notebook.png");
        dto.setOrden(1);
        dto.setPrincipal(true);

        ImagenProducto guardada = new ImagenProducto();
        guardada.setId(100L);
        guardada.setUrl(dto.getUrl());
        guardada.setOrden(dto.getOrden());
        guardada.setPrincipal(dto.getPrincipal());
        guardada.setProducto(producto);

        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(imagenProductoRepository.save(any(ImagenProducto.class))).thenReturn(guardada);

        ImagenProductoResponseDTO resultado = productoService.agregarImagen(10L, dto);

        assertEquals(100L, resultado.getId());
        assertEquals("https://img.test/notebook.png", resultado.getUrl());
        assertEquals(10L, resultado.getProductoId());
        verify(imagenProductoRepository).save(any(ImagenProducto.class));
    }

    @Test
    void listarTodos_devuelveProductos() {
        Producto producto = crearProducto(10L, crearCategoria(1L), crearMarca(2L));
        when(productoRepository.findAll()).thenReturn(List.of(producto));

        List<ProductoResponseDTO> resultado = productoService.listarTodos();

        assertEquals(1, resultado.size());
        assertEquals(10L, resultado.get(0).getId());
        verify(productoRepository).findAll();
    }

    private Producto crearProducto(Long id, Categoria categoria, Marca marca) {
        Producto producto = new Producto();
        producto.setId(id);
        producto.setNombre("Notebook");
        producto.setPrecio(799990.0);
        producto.setStock(12);
        producto.setSku("NB-001");
        producto.setCategoria(categoria);
        producto.setMarca(marca);
        return producto;
    }

    private Categoria crearCategoria(Long id) {
        Categoria categoria = new Categoria();
        categoria.setId(id);
        categoria.setNombre("Computacion");
        return categoria;
    }

    private Marca crearMarca(Long id) {
        Marca marca = new Marca();
        marca.setId(id);
        marca.setNombre("Lenovo");
        return marca;
    }
}
