package com.shopconnect.ms_productos.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopconnect.ms_productos.model.Categoria;
import com.shopconnect.ms_productos.model.ImagenProducto;
import com.shopconnect.ms_productos.model.Marca;
import com.shopconnect.ms_productos.model.Producto;
import com.shopconnect.ms_productos.repository.CategoriaRepository;
import com.shopconnect.ms_productos.repository.ImagenProductoRepository;
import com.shopconnect.ms_productos.repository.MarcaRepository;
import com.shopconnect.ms_productos.repository.ProductoRepository;

/**
 * SERVICIO: ProductoService
 *
 * RESPONSABILIDAD: Lógica de negocio.
 * El Service llama a los métodos del Repository.
 * El Service NO escribe SQL ni usa EntityManager directamente.
 */
@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private MarcaRepository marcaRepository;

    @Autowired
    private ImagenProductoRepository imagenProductoRepository;

    // ═══ PRODUCTOS ════════════════════════════════════════════════════

    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    public Optional<Producto> buscarPorId(Long id) {
        return productoRepository.findById(id);
    }

    public List<Producto> buscarPorCategoria(Long categoriaId) {
        return productoRepository.findByCategoriaId(categoriaId);
    }

    public List<Producto> buscarPorMarca(Long marcaId) {
        return productoRepository.findByMarcaId(marcaId);
    }

    public List<Producto> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    @Transactional
    public Producto crear(Producto producto, Long categoriaId, Long marcaId) {

        // Validar SKU único
        if (productoRepository.existsBySku(producto.getSku())) {
            throw new IllegalArgumentException("SKU duplicado: " + producto.getSku());
        }

        // Validar que existe la categoría
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada: " + categoriaId));
        producto.setCategoria(categoria);

        // Validar que existe la marca
        Marca marca = marcaRepository.findById(marcaId)
                .orElseThrow(() -> new RuntimeException("Marca no encontrada: " + marcaId));
        producto.setMarca(marca);

        return productoRepository.save(producto);
    }

    @Transactional
    public Producto actualizar(Long id, Producto datos) {

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + id));

        if (datos.getNombre() != null) {
            producto.setNombre(datos.getNombre());
        }

        if (datos.getPrecio() != null) {
            producto.setPrecio(datos.getPrecio());
        }

        if (datos.getStock() != null) {
            producto.setStock(datos.getStock());
        }

        if (datos.getSku() != null) {
            if (!datos.getSku().equals(producto.getSku()) &&
                    productoRepository.existsBySku(datos.getSku())) {
                throw new IllegalArgumentException("SKU duplicado: " + datos.getSku());
            }

            producto.setSku(datos.getSku());
        }

        return productoRepository.save(producto);
    }

    @Transactional
    public void eliminar(Long id) {

        if (!productoRepository.existsById(id)) {
            throw new RuntimeException("Producto no encontrado: " + id);
        }

        productoRepository.deleteById(id);
    }

    // ═══ CATEGORÍAS ════════════════════════════════════════════════════

    public List<Categoria> listarCategorias() {
        return categoriaRepository.findAll();
    }

    public Optional<Categoria> buscarCategoriaPorId(Long id) {
        return categoriaRepository.findById(id);
    }

    @Transactional
    public Categoria crearCategoria(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public void eliminarCategoria(Long id) {

        if (!categoriaRepository.existsById(id)) {
            throw new RuntimeException("Categoría no encontrada: " + id);
        }

        categoriaRepository.deleteById(id);
    }

    // ═══ MARCAS ════════════════════════════════════════════════════════

    public List<Marca> listarMarcas() {
        return marcaRepository.findAll();
    }

    public Optional<Marca> buscarMarcaPorId(Long id) {
        return marcaRepository.findById(id);
    }

    @Transactional
    public Marca crearMarca(Marca marca) {
        return marcaRepository.save(marca);
    }

    @Transactional
    public void eliminarMarca(Long id) {

        if (!marcaRepository.existsById(id)) {
            throw new RuntimeException("Marca no encontrada: " + id);
        }

        marcaRepository.deleteById(id);
    }

    // ═══ IMÁGENES DE PRODUCTO ══════════════════════════════════════════

    public List<ImagenProducto> listarImagenesPorProducto(Long productoId) {
        return imagenProductoRepository.findByProductoId(productoId);
    }

    @Transactional
    public ImagenProducto agregarImagen(Long productoId, ImagenProducto imagenProducto) {

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + productoId));

        imagenProducto.setProducto(producto);

        return imagenProductoRepository.save(imagenProducto);
    }

    @Transactional
    public void eliminarImagen(Long id) {

        if (!imagenProductoRepository.existsById(id)) {
            throw new RuntimeException("Imagen no encontrada: " + id);
        }

        imagenProductoRepository.deleteById(id);
    }

    // ═══ OPERACIONES ESPECÍFICAS ═══════════════════════════════════════

    @Transactional
    public Producto actualizarCategoria(Long productoId, Long categoriaId) {

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + productoId));

        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada: " + categoriaId));

        producto.setCategoria(categoria);

        return productoRepository.save(producto);
    }

    @Transactional
    public Producto actualizarMarca(Long productoId, Long marcaId) {

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + productoId));

        Marca marca = marcaRepository.findById(marcaId)
                .orElseThrow(() -> new RuntimeException("Marca no encontrada: " + marcaId));

        producto.setMarca(marca);

        return productoRepository.save(producto);
    }
}