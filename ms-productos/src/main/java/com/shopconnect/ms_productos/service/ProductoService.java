package com.shopconnect.ms_productos.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopconnect.ms_productos.dto.request.CategoriaRequestDTO;
import com.shopconnect.ms_productos.dto.request.ImagenProductoRequestDTO;
import com.shopconnect.ms_productos.dto.request.MarcaRequestDTO;
import com.shopconnect.ms_productos.dto.request.ProductoRequestDTO;
import com.shopconnect.ms_productos.dto.response.CategoriaResponseDTO;
import com.shopconnect.ms_productos.dto.response.ImagenProductoResponseDTO;
import com.shopconnect.ms_productos.dto.response.MarcaResponseDTO;
import com.shopconnect.ms_productos.dto.response.ProductoResponseDTO;
import com.shopconnect.ms_productos.model.Categoria;
import com.shopconnect.ms_productos.model.ImagenProducto;
import com.shopconnect.ms_productos.model.Marca;
import com.shopconnect.ms_productos.model.Producto;
import com.shopconnect.ms_productos.repository.CategoriaRepository;
import com.shopconnect.ms_productos.repository.ImagenProductoRepository;
import com.shopconnect.ms_productos.repository.MarcaRepository;
import com.shopconnect.ms_productos.repository.ProductoRepository;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MarcaRepository marcaRepository;
    private final ImagenProductoRepository imagenProductoRepository;

    public ProductoService(ProductoRepository productoRepository,
                           CategoriaRepository categoriaRepository,
                           MarcaRepository marcaRepository,
                           ImagenProductoRepository imagenProductoRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.marcaRepository = marcaRepository;
        this.imagenProductoRepository = imagenProductoRepository;
    }

    public List<ProductoResponseDTO> listarTodos() {
        return productoRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public ProductoResponseDTO buscarPorId(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + id));
        return convertirADTO(producto);
    }

    public List<ProductoResponseDTO> buscarPorCategoria(Long categoriaId) {
        return productoRepository.findByCategoriaId(categoriaId)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public List<ProductoResponseDTO> buscarPorMarca(Long marcaId) {
        return productoRepository.findByMarcaId(marcaId)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public List<ProductoResponseDTO> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional
    public ProductoResponseDTO crear(ProductoRequestDTO dto) {

        if (productoRepository.existsBySku(dto.getSku())) {
            throw new IllegalArgumentException("SKU duplicado: " + dto.getSku());
        }

        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada: " + dto.getCategoriaId()));

        Marca marca = marcaRepository.findById(dto.getMarcaId())
                .orElseThrow(() -> new RuntimeException("Marca no encontrada: " + dto.getMarcaId()));

        Producto producto = new Producto();
        producto.setNombre(dto.getNombre());
        producto.setPrecio(dto.getPrecio());
        producto.setStock(dto.getStock());
        producto.setSku(dto.getSku());
        producto.setCategoria(categoria);
        producto.setMarca(marca);

        return convertirADTO(productoRepository.save(producto));
    }

    @Transactional
    public ProductoResponseDTO actualizar(Long id, ProductoRequestDTO dto) {

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + id));

        if (dto.getNombre() != null) {
            producto.setNombre(dto.getNombre());
        }

        if (dto.getPrecio() != null) {
            producto.setPrecio(dto.getPrecio());
        }

        if (dto.getStock() != null) {
            producto.setStock(dto.getStock());
        }

        if (dto.getSku() != null) {
            if (!dto.getSku().equals(producto.getSku()) && productoRepository.existsBySku(dto.getSku())) {
                throw new IllegalArgumentException("SKU duplicado: " + dto.getSku());
            }
            producto.setSku(dto.getSku());
        }

        if (dto.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada: " + dto.getCategoriaId()));
            producto.setCategoria(categoria);
        }

        if (dto.getMarcaId() != null) {
            Marca marca = marcaRepository.findById(dto.getMarcaId())
                    .orElseThrow(() -> new RuntimeException("Marca no encontrada: " + dto.getMarcaId()));
            producto.setMarca(marca);
        }

        return convertirADTO(productoRepository.save(producto));
    }

    @Transactional
    public void eliminar(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + id));

        productoRepository.delete(producto);
    }

    public List<CategoriaResponseDTO> listarCategorias() {
        return categoriaRepository.findAll()
                .stream()
                .map(this::convertirCategoriaADTO)
                .toList();
    }

    public CategoriaResponseDTO buscarCategoriaPorId(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada: " + id));
        return convertirCategoriaADTO(categoria);
    }

    @Transactional
    public CategoriaResponseDTO crearCategoria(CategoriaRequestDTO dto) {
        Categoria categoria = new Categoria();
        categoria.setNombre(dto.getNombre());
        categoria.setDescripcion(dto.getDescripcion());
        categoria.setActiva(dto.getActiva());

        return convertirCategoriaADTO(categoriaRepository.save(categoria));
    }

    @Transactional
    public void eliminarCategoria(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada: " + id));

        categoriaRepository.delete(categoria);
    }

    public List<MarcaResponseDTO> listarMarcas() {
        return marcaRepository.findAll()
                .stream()
                .map(this::convertirMarcaADTO)
                .toList();
    }

    public MarcaResponseDTO buscarMarcaPorId(Long id) {
        Marca marca = marcaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Marca no encontrada: " + id));
        return convertirMarcaADTO(marca);
    }

    @Transactional
    public MarcaResponseDTO crearMarca(MarcaRequestDTO dto) {
        Marca marca = new Marca();
        marca.setNombre(dto.getNombre());
        marca.setPaisOrigen(dto.getPaisOrigen());
        marca.setLogoUrl(dto.getLogoUrl());

        return convertirMarcaADTO(marcaRepository.save(marca));
    }

    @Transactional
    public void eliminarMarca(Long id) {
        Marca marca = marcaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Marca no encontrada: " + id));

        marcaRepository.delete(marca);
    }

    public List<ImagenProductoResponseDTO> listarImagenesPorProducto(Long productoId) {
        return imagenProductoRepository.findByProductoId(productoId)
                .stream()
                .map(this::convertirImagenADTO)
                .toList();
    }

    @Transactional
    public ImagenProductoResponseDTO agregarImagen(Long productoId, ImagenProductoRequestDTO dto) {

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + productoId));

        ImagenProducto imagen = new ImagenProducto();
        imagen.setUrl(dto.getUrl());
        imagen.setOrden(dto.getOrden());
        imagen.setPrincipal(dto.getPrincipal());
        imagen.setProducto(producto);

        return convertirImagenADTO(imagenProductoRepository.save(imagen));
    }

    @Transactional
    public void eliminarImagen(Long id) {
        ImagenProducto imagen = imagenProductoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Imagen no encontrada: " + id));

        imagenProductoRepository.delete(imagen);
    }

    @Transactional
    public ProductoResponseDTO actualizarCategoria(Long productoId, Long categoriaId) {

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + productoId));

        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada: " + categoriaId));

        producto.setCategoria(categoria);

        return convertirADTO(productoRepository.save(producto));
    }

    @Transactional
    public ProductoResponseDTO actualizarMarca(Long productoId, Long marcaId) {

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + productoId));

        Marca marca = marcaRepository.findById(marcaId)
                .orElseThrow(() -> new RuntimeException("Marca no encontrada: " + marcaId));

        producto.setMarca(marca);

        return convertirADTO(productoRepository.save(producto));
    }

    private Producto convertirAEntity(ProductoRequestDTO dto) {
        Producto producto = new Producto();
        producto.setNombre(dto.getNombre());
        producto.setPrecio(dto.getPrecio());
        producto.setStock(dto.getStock());
        producto.setSku(dto.getSku());
        return producto;
    }

    private ProductoResponseDTO convertirADTO(Producto producto) {
        ProductoResponseDTO dto = new ProductoResponseDTO();
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setPrecio(producto.getPrecio());
        dto.setStock(producto.getStock());
        dto.setSku(producto.getSku());
        dto.setCategoriaId(producto.getCategoria() != null ? producto.getCategoria().getId() : null);
        dto.setMarcaId(producto.getMarca() != null ? producto.getMarca().getId() : null);
        return dto;
    }

    private CategoriaResponseDTO convertirCategoriaADTO(Categoria categoria) {
        CategoriaResponseDTO dto = new CategoriaResponseDTO();
        dto.setId(categoria.getId());
        dto.setNombre(categoria.getNombre());
        dto.setDescripcion(categoria.getDescripcion());
        dto.setActiva(categoria.getActiva());
        return dto;
    }

    private MarcaResponseDTO convertirMarcaADTO(Marca marca) {
        MarcaResponseDTO dto = new MarcaResponseDTO();
        dto.setId(marca.getId());
        dto.setNombre(marca.getNombre());
        dto.setPaisOrigen(marca.getPaisOrigen());
        dto.setLogoUrl(marca.getLogoUrl());
        return dto;
    }

    private ImagenProductoResponseDTO convertirImagenADTO(ImagenProducto imagen) {
        ImagenProductoResponseDTO dto = new ImagenProductoResponseDTO();
        dto.setId(imagen.getId());
        dto.setUrl(imagen.getUrl());
        dto.setOrden(imagen.getOrden());
        dto.setPrincipal(imagen.getPrincipal());
        dto.setProductoId(imagen.getProducto() != null ? imagen.getProducto().getId() : null);
        return dto;
    }
}