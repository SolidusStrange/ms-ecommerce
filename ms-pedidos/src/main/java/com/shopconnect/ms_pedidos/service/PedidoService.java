package com.shopconnect.ms_pedidos.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.shopconnect.ms_pedidos.dto.request.DetallePedidoRequestDTO;
import com.shopconnect.ms_pedidos.dto.request.EstadoPedidoRequestDTO;
import com.shopconnect.ms_pedidos.dto.request.PedidoRequestDTO;
import com.shopconnect.ms_pedidos.dto.response.DetallePedidoResponseDTO;
import com.shopconnect.ms_pedidos.dto.response.EstadoPedidoResponseDTO;
import com.shopconnect.ms_pedidos.dto.response.PedidoResponseDTO;
import com.shopconnect.ms_pedidos.model.DetallePedido;
import com.shopconnect.ms_pedidos.model.EstadoPedido;
import com.shopconnect.ms_pedidos.model.Pedido;
import com.shopconnect.ms_pedidos.repository.DetallePedidoRepository;
import com.shopconnect.ms_pedidos.repository.EstadoPedidoRepository;
import com.shopconnect.ms_pedidos.repository.PedidoRepository;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final EstadoPedidoRepository estadoPedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final RestTemplate restTemplate;

    @Value("${app.ms-usuarios.url}")
    private String usuariosBaseUrl;

    @Value("${app.ms-productos.url}")
    private String productosBaseUrl;

    public PedidoService(PedidoRepository pedidoRepository,
                         EstadoPedidoRepository estadoPedidoRepository,
                         DetallePedidoRepository detallePedidoRepository,
                         RestTemplate restTemplate) {
        this.pedidoRepository = pedidoRepository;
        this.estadoPedidoRepository = estadoPedidoRepository;
        this.detallePedidoRepository = detallePedidoRepository;
        this.restTemplate = restTemplate;
    }

    public List<PedidoResponseDTO> listarTodos() {
        return pedidoRepository.findAll()
                .stream()
                .map(this::convertirAPedidoDTO)
                .toList();
    }

    public Optional<PedidoResponseDTO> buscarPorId(Long id) {
        return pedidoRepository.findById(id)
                .map(this::convertirAPedidoDTO);
    }

    public List<PedidoResponseDTO> listarPorUsuario(Long usuarioId) {
        return pedidoRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(this::convertirAPedidoDTO)
                .toList();
    }

    public List<PedidoResponseDTO> listarPorEstado(Long estadoId) {
        return pedidoRepository.findByEstadoId(estadoId)
                .stream()
                .map(this::convertirAPedidoDTO)
                .toList();
    }

    @Transactional
    public PedidoResponseDTO crear(PedidoRequestDTO dto) {
        validarUsuarioExiste(dto.getUsuarioId());

        EstadoPedido estado = estadoPedidoRepository.findById(dto.getEstadoId())
                .orElseThrow(() -> new RuntimeException("Estado no encontrado: " + dto.getEstadoId()));

        Pedido pedido = new Pedido();
        pedido.setUsuarioId(dto.getUsuarioId());
        pedido.setEstado(estado);
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setTotal(dto.getTotal() != null ? dto.getTotal() : 0.0);

        return convertirAPedidoDTO(pedidoRepository.save(pedido));
    }

    @Transactional
    public PedidoResponseDTO actualizar(Long id, PedidoRequestDTO dto) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + id));

        if (dto.getUsuarioId() != null) {
            validarUsuarioExiste(dto.getUsuarioId());
            pedido.setUsuarioId(dto.getUsuarioId());
        }

        if (dto.getEstadoId() != null) {
            EstadoPedido estado = estadoPedidoRepository.findById(dto.getEstadoId())
                    .orElseThrow(() -> new RuntimeException("Estado no encontrado: " + dto.getEstadoId()));
            pedido.setEstado(estado);
        }

        if (dto.getTotal() != null) {
            pedido.setTotal(dto.getTotal());
        }

        return convertirAPedidoDTO(pedidoRepository.save(pedido));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!pedidoRepository.existsById(id)) {
            throw new RuntimeException("Pedido no encontrado: " + id);
        }
        pedidoRepository.deleteById(id);
    }

    public List<EstadoPedidoResponseDTO> listarEstados() {
        return estadoPedidoRepository.findAll()
                .stream()
                .map(this::convertirAEstadoDTO)
                .toList();
    }

    public Optional<EstadoPedidoResponseDTO> buscarEstadoPorId(Long id) {
        return estadoPedidoRepository.findById(id)
                .map(this::convertirAEstadoDTO);
    }

    @Transactional
    public EstadoPedidoResponseDTO crearEstado(EstadoPedidoRequestDTO dto) {
        if (estadoPedidoRepository.existsByNombre(dto.getNombre())) {
            throw new IllegalArgumentException("Nombre de estado duplicado: " + dto.getNombre());
        }

        EstadoPedido estado = new EstadoPedido();
        estado.setNombre(dto.getNombre());
        estado.setDescripcion(dto.getDescripcion());

        return convertirAEstadoDTO(estadoPedidoRepository.save(estado));
    }

    @Transactional
    public void eliminarEstado(Long id) {
        if (!estadoPedidoRepository.existsById(id)) {
            throw new RuntimeException("Estado no encontrado: " + id);
        }

        List<Pedido> pedidosConEstado = pedidoRepository.findByEstadoId(id);
        if (!pedidosConEstado.isEmpty()) {
            throw new IllegalStateException("El estado tiene pedidos asociados");
        }

        estadoPedidoRepository.deleteById(id);
    }

    public List<DetallePedidoResponseDTO> listarDetallesPorPedido(Long pedidoId) {
        return detallePedidoRepository.findByPedidoId(pedidoId)
                .stream()
                .map(this::convertirADetalleDTO)
                .toList();
    }

    public List<DetallePedidoResponseDTO> listarDetallesPorProducto(Long productoId) {
        return detallePedidoRepository.findByProductoId(productoId)
                .stream()
                .map(this::convertirADetalleDTO)
                .toList();
    }

    @Transactional
    public DetallePedidoResponseDTO agregarDetalle(Long pedidoId, DetallePedidoRequestDTO dto) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + pedidoId));

        validarProductoExiste(dto.getProductoId());

        DetallePedido detalle = new DetallePedido();
        detalle.setCantidad(dto.getCantidad());
        detalle.setPrecioUnit(dto.getPrecioUnit());
        detalle.setProductoId(dto.getProductoId());
        detalle.setPedido(pedido);

        DetallePedido detalleGuardado = detallePedidoRepository.save(detalle);
        recalcularTotal(pedido);

        return convertirADetalleDTO(detalleGuardado);
    }

    @Transactional
    public void eliminarDetalle(Long id) {
        DetallePedido detalle = detallePedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Detalle no encontrado: " + id));

        Pedido pedido = detalle.getPedido();
        detallePedidoRepository.deleteById(id);
        recalcularTotal(pedido);
    }

    @Transactional
    public PedidoResponseDTO cambiarEstado(Long pedidoId, Long estadoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + pedidoId));

        EstadoPedido estado = estadoPedidoRepository.findById(estadoId)
                .orElseThrow(() -> new RuntimeException("Estado no encontrado: " + estadoId));

        pedido.setEstado(estado);

        return convertirAPedidoDTO(pedidoRepository.save(pedido));
    }

    private void validarUsuarioExiste(Long usuarioId) {
        try {
            ResponseEntity<Object> response = restTemplate.getForEntity(
                    usuariosBaseUrl + "/" + usuarioId,
                    Object.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Usuario no encontrado: " + usuarioId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Usuario no encontrado: " + usuarioId);
        }
    }

    private void validarProductoExiste(Long productoId) {
        try {
            ResponseEntity<Object> response = restTemplate.getForEntity(
                    productosBaseUrl + "/" + productoId,
                    Object.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Producto no encontrado: " + productoId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Producto no encontrado: " + productoId);
        }
    }

    private void recalcularTotal(Pedido pedido) {
        List<DetallePedido> detalles = detallePedidoRepository.findByPedidoId(pedido.getId());

        double total = 0.0;
        for (DetallePedido detalle : detalles) {
            total += detalle.getCantidad() * detalle.getPrecioUnit();
        }

        pedido.setTotal(total);
        pedidoRepository.save(pedido);
    }

    private PedidoResponseDTO convertirAPedidoDTO(Pedido pedido) {
        PedidoResponseDTO dto = new PedidoResponseDTO();
        dto.setId(pedido.getId());
        dto.setFechaPedido(pedido.getFechaPedido());
        dto.setTotal(pedido.getTotal());
        dto.setUsuarioId(pedido.getUsuarioId());
        dto.setEstadoId(pedido.getEstado() != null ? pedido.getEstado().getId() : null);
        return dto;
    }

    private EstadoPedidoResponseDTO convertirAEstadoDTO(EstadoPedido estado) {
        EstadoPedidoResponseDTO dto = new EstadoPedidoResponseDTO();
        dto.setId(estado.getId());
        dto.setNombre(estado.getNombre());
        dto.setDescripcion(estado.getDescripcion());
        return dto;
    }

    private DetallePedidoResponseDTO convertirADetalleDTO(DetallePedido detalle) {
        DetallePedidoResponseDTO dto = new DetallePedidoResponseDTO();
        dto.setId(detalle.getId());
        dto.setCantidad(detalle.getCantidad());
        dto.setPrecioUnit(detalle.getPrecioUnit());
        dto.setProductoId(detalle.getProductoId());
        dto.setPedidoId(detalle.getPedido() != null ? detalle.getPedido().getId() : null);
        return dto;
    }
}