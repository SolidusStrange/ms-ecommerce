package com.shopconnect.ms_inventario.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.shopconnect.ms_inventario.dto.request.InventarioRequestDTO;
import com.shopconnect.ms_inventario.dto.request.MovimientoInventarioRequestDTO;
import com.shopconnect.ms_inventario.dto.response.InventarioResponseDTO;
import com.shopconnect.ms_inventario.dto.response.MovimientoInventarioResponseDTO;
import com.shopconnect.ms_inventario.model.Inventario;
import com.shopconnect.ms_inventario.model.MovimientoInventario;
import com.shopconnect.ms_inventario.repository.InventarioRepository;
import com.shopconnect.ms_inventario.repository.MovimientoInventarioRepository;

@Service
public class InventarioService {

    private final InventarioRepository inventarioRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final RestTemplate restTemplate;

    @Value("${servicios.productos.url}")
    private String productosUrl;

    public InventarioService(InventarioRepository inventarioRepository,
                             MovimientoInventarioRepository movimientoInventarioRepository,
                             RestTemplate restTemplate) {
        this.inventarioRepository = inventarioRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
        this.restTemplate = restTemplate;
    }

    public List<InventarioResponseDTO> listar() {
        return inventarioRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public InventarioResponseDTO buscarPorId(Long id) {
        Inventario inventario = inventarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));
        return convertirADTO(inventario);
    }

    public InventarioResponseDTO buscarPorProductoId(Long productoId) {
        Inventario inventario = inventarioRepository.findByProductoId(productoId)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));
        return convertirADTO(inventario);
    }

    @Transactional
    public InventarioResponseDTO crear(InventarioRequestDTO dto) {
        validarProducto(dto.getProductoId());

        if (inventarioRepository.existsByProductoId(dto.getProductoId())) {
            throw new RuntimeException("Ya existe inventario para el productoId: " + dto.getProductoId());
        }

        Inventario inventario = convertirAEntity(dto);
        return convertirADTO(inventarioRepository.save(inventario));
    }

    @Transactional
    public InventarioResponseDTO actualizar(Long id, InventarioRequestDTO dto) {
        validarProducto(dto.getProductoId());

        Inventario inventario = inventarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));

        if (!inventario.getProductoId().equals(dto.getProductoId())
                && inventarioRepository.existsByProductoId(dto.getProductoId())) {
            throw new RuntimeException("Ya existe inventario para el productoId: " + dto.getProductoId());
        }

        inventario.setProductoId(dto.getProductoId());
        inventario.setStockActual(dto.getStockActual());
        inventario.setStockMinimo(dto.getStockMinimo());

        return convertirADTO(inventarioRepository.save(inventario));
    }

    @Transactional
    public void eliminar(Long id) {
        Inventario inventario = inventarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));

        inventarioRepository.delete(inventario);
    }

    public List<MovimientoInventarioResponseDTO> listarMovimientosPorInventario(Long inventarioId) {
        return movimientoInventarioRepository.findByInventarioId(inventarioId)
                .stream()
                .map(this::convertirMovimientoADTO)
                .toList();
    }

    @Transactional
    public MovimientoInventarioResponseDTO registrarMovimiento(Long inventarioId,
                                                               MovimientoInventarioRequestDTO dto) {
        Inventario inventario = inventarioRepository.findById(inventarioId)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));

        if (dto.getCantidad() == null || dto.getCantidad() <= 0) {
            throw new RuntimeException("La cantidad debe ser mayor a 0");
        }

        if (dto.getTipo() == null || dto.getTipo().isBlank()) {
            throw new RuntimeException("El tipo de movimiento es obligatorio");
        }

        String tipo = dto.getTipo().toUpperCase();

        if (tipo.equals("ENTRADA")) {
            inventario.setStockActual(inventario.getStockActual() + dto.getCantidad());
        } else if (tipo.equals("SALIDA")) {
            if (inventario.getStockActual() < dto.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para realizar la salida");
            }
            inventario.setStockActual(inventario.getStockActual() - dto.getCantidad());
        } else {
            throw new RuntimeException("Tipo de movimiento inválido");
        }

        inventarioRepository.save(inventario);

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setTipo(tipo);
        movimiento.setCantidad(dto.getCantidad());
        movimiento.setFechaMovimiento(LocalDateTime.now());
        movimiento.setInventario(inventario);

        return convertirMovimientoADTO(movimientoInventarioRepository.save(movimiento));
    }

    @Transactional
    public InventarioResponseDTO ajustarStockMinimo(Long inventarioId, Integer stockMinimo) {
        Inventario inventario = inventarioRepository.findById(inventarioId)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));

        if (stockMinimo == null || stockMinimo < 0) {
            throw new RuntimeException("El stock mínimo no puede ser negativo");
        }

        inventario.setStockMinimo(stockMinimo);
        return convertirADTO(inventarioRepository.save(inventario));
    }

    private void validarProducto(Long productoId) {
        try {
            restTemplate.getForObject(productosUrl + "/api/v1/productos/" + productoId, Object.class);
        } catch (Exception e) {
            throw new RuntimeException("Producto no encontrado: " + productoId);
        }
    }

    private Inventario convertirAEntity(InventarioRequestDTO dto) {
        Inventario inventario = new Inventario();
        inventario.setProductoId(dto.getProductoId());
        inventario.setStockActual(dto.getStockActual());
        inventario.setStockMinimo(dto.getStockMinimo());
        return inventario;
    }

    private InventarioResponseDTO convertirADTO(Inventario inventario) {
        InventarioResponseDTO dto = new InventarioResponseDTO();
        dto.setId(inventario.getId());
        dto.setProductoId(inventario.getProductoId());
        dto.setStockActual(inventario.getStockActual());
        dto.setStockMinimo(inventario.getStockMinimo());
        return dto;
    }

    private MovimientoInventarioResponseDTO convertirMovimientoADTO(MovimientoInventario movimiento) {
        MovimientoInventarioResponseDTO dto = new MovimientoInventarioResponseDTO();
        dto.setId(movimiento.getId());
        dto.setTipo(movimiento.getTipo());
        dto.setCantidad(movimiento.getCantidad());
        dto.setFechaMovimiento(movimiento.getFechaMovimiento());
        dto.setInventarioId(movimiento.getInventario().getId());
        return dto;
    }
}