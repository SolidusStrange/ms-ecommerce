package com.shopconnect.ms_pagos.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.shopconnect.ms_pagos.dto.request.MetodoPagoRequestDTO;
import com.shopconnect.ms_pagos.dto.request.PagoRequestDTO;
import com.shopconnect.ms_pagos.dto.request.TransaccionPagoRequestDTO;
import com.shopconnect.ms_pagos.dto.response.MetodoPagoResponseDTO;
import com.shopconnect.ms_pagos.dto.response.PagoResponseDTO;
import com.shopconnect.ms_pagos.dto.response.TransaccionPagoResponseDTO;
import com.shopconnect.ms_pagos.model.MetodoPago;
import com.shopconnect.ms_pagos.model.Pago;
import com.shopconnect.ms_pagos.model.TransaccionPago;
import com.shopconnect.ms_pagos.repository.MetodoPagoRepository;
import com.shopconnect.ms_pagos.repository.PagoRepository;
import com.shopconnect.ms_pagos.repository.TransaccionPagoRepository;

@Service
public class PagoService {

    private final PagoRepository pagoRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final TransaccionPagoRepository transaccionPagoRepository;
    private final RestTemplate restTemplate;

    @Value("${app.ms-pedidos.url}")
    private String pedidosBaseUrl;

    public PagoService(PagoRepository pagoRepository,
                       MetodoPagoRepository metodoPagoRepository,
                       TransaccionPagoRepository transaccionPagoRepository,
                       RestTemplate restTemplate) {
        this.pagoRepository = pagoRepository;
        this.metodoPagoRepository = metodoPagoRepository;
        this.transaccionPagoRepository = transaccionPagoRepository;
        this.restTemplate = restTemplate;
    }

    // ═══ PAGOS ════════════════════════════════════════════════════

    public List<PagoResponseDTO> listarTodos() {
        return pagoRepository.findAll()
                .stream()
                .map(this::convertirAPagoDTO)
                .toList();
    }

    public Optional<PagoResponseDTO> buscarPorId(Long id) {
        return pagoRepository.findById(id)
                .map(this::convertirAPagoDTO);
    }

    public List<PagoResponseDTO> listarPorPedido(Long pedidoId) {
        return pagoRepository.findByPedidoId(pedidoId)
                .stream()
                .map(this::convertirAPagoDTO)
                .toList();
    }

    public List<PagoResponseDTO> listarPorEstado(String estado) {
        return pagoRepository.findByEstado(estado.toUpperCase())
                .stream()
                .map(this::convertirAPagoDTO)
                .toList();
    }

    @Transactional
    public PagoResponseDTO procesar(PagoRequestDTO dto) {
        validarPedidoExiste(dto.getPedidoId());

        MetodoPago metodoPago = metodoPagoRepository.findById(dto.getMetodoPagoId())
                .orElseThrow(() -> new RuntimeException("Método de pago no encontrado: " + dto.getMetodoPagoId()));

        if (metodoPago.getActivo() != null && !metodoPago.getActivo()) {
            throw new IllegalStateException("El método de pago no está activo");
        }

        Pago pago = new Pago();
        pago.setMonto(dto.getMonto());
        pago.setPedidoId(dto.getPedidoId());
        pago.setEstado(dto.getEstado() == null || dto.getEstado().isBlank()
                ? "PENDIENTE"
                : dto.getEstado().toUpperCase());
        pago.setFechaPago(LocalDateTime.now());
        pago.setMetodoPago(metodoPago);

        Pago pagoGuardado = pagoRepository.save(pago);

        TransaccionPago transaccion = new TransaccionPago();
        transaccion.setPago(pagoGuardado);
        transaccion.setEstado("PENDIENTE");
        transaccion.setFechaTransaccion(LocalDateTime.now());
        transaccion.setCodigoTransaccion("TX-" + pagoGuardado.getId() + "-" + System.currentTimeMillis());
        transaccionPagoRepository.save(transaccion);

        return convertirAPagoDTO(pagoGuardado);
    }

    @Transactional
    public PagoResponseDTO actualizar(Long id, PagoRequestDTO dto) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado: " + id));

        if (dto.getMonto() != null) {
            pago.setMonto(dto.getMonto());
        }

        if (dto.getPedidoId() != null) {
            validarPedidoExiste(dto.getPedidoId());
            pago.setPedidoId(dto.getPedidoId());
        }

        if (dto.getEstado() != null && !dto.getEstado().isBlank()) {
            pago.setEstado(dto.getEstado().toUpperCase());
        }

        if (dto.getMetodoPagoId() != null) {
            MetodoPago metodoPago = metodoPagoRepository.findById(dto.getMetodoPagoId())
                    .orElseThrow(() -> new RuntimeException("Método de pago no encontrado: " + dto.getMetodoPagoId()));

            if (metodoPago.getActivo() != null && !metodoPago.getActivo()) {
                throw new IllegalStateException("El método de pago no está activo");
            }

            pago.setMetodoPago(metodoPago);
        }

        return convertirAPagoDTO(pagoRepository.save(pago));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!pagoRepository.existsById(id)) {
            throw new RuntimeException("Pago no encontrado: " + id);
        }
        pagoRepository.deleteById(id);
    }

    @Transactional
    public PagoResponseDTO actualizarEstado(Long pagoId, String estado) {
        Pago pago = pagoRepository.findById(pagoId)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado: " + pagoId));

        if (estado == null || estado.isBlank()) {
            throw new IllegalArgumentException("El estado es obligatorio");
        }

        pago.setEstado(estado.toUpperCase());
        return convertirAPagoDTO(pagoRepository.save(pago));
    }

    // ═══ MÉTODOS DE PAGO ═════════════════════════════════════════════

    public List<MetodoPagoResponseDTO> listarMetodos() {
        return metodoPagoRepository.findAll()
                .stream()
                .map(this::convertirAMetodoDTO)
                .toList();
    }

    public List<MetodoPagoResponseDTO> listarMetodosActivos() {
        return metodoPagoRepository.findByActivo(true)
                .stream()
                .map(this::convertirAMetodoDTO)
                .toList();
    }

    public Optional<MetodoPagoResponseDTO> buscarMetodoPorId(Long id) {
        return metodoPagoRepository.findById(id)
                .map(this::convertirAMetodoDTO);
    }

    @Transactional
    public MetodoPagoResponseDTO crearMetodo(MetodoPagoRequestDTO dto) {
        if (metodoPagoRepository.existsByNombre(dto.getNombre())) {
            throw new IllegalArgumentException("Nombre de método de pago duplicado: " + dto.getNombre());
        }

        MetodoPago metodoPago = new MetodoPago();
        metodoPago.setNombre(dto.getNombre());
        metodoPago.setActivo(dto.getActivo() == null ? true : dto.getActivo());

        return convertirAMetodoDTO(metodoPagoRepository.save(metodoPago));
    }

    @Transactional
    public MetodoPagoResponseDTO actualizarMetodo(Long id, MetodoPagoRequestDTO dto) {
        MetodoPago metodoPago = metodoPagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Método de pago no encontrado: " + id));

        if (dto.getNombre() != null) {
            if (!dto.getNombre().equals(metodoPago.getNombre())
                    && metodoPagoRepository.existsByNombre(dto.getNombre())) {
                throw new IllegalArgumentException("Nombre de método de pago duplicado: " + dto.getNombre());
            }
            metodoPago.setNombre(dto.getNombre());
        }

        if (dto.getActivo() != null) {
            metodoPago.setActivo(dto.getActivo());
        }

        return convertirAMetodoDTO(metodoPagoRepository.save(metodoPago));
    }

    @Transactional
    public void eliminarMetodo(Long id) {
        if (!metodoPagoRepository.existsById(id)) {
            throw new RuntimeException("Método de pago no encontrado: " + id);
        }
        metodoPagoRepository.deleteById(id);
    }

    // ═══ TRANSACCIONES DE PAGO ═══════════════════════════════════════

    public List<TransaccionPagoResponseDTO> listarTransaccionesPorPago(Long pagoId) {
        return transaccionPagoRepository.findByPagoId(pagoId)
                .stream()
                .map(this::convertirATransaccionDTO)
                .toList();
    }

    @Transactional
    public TransaccionPagoResponseDTO agregarTransaccion(Long pagoId, TransaccionPagoRequestDTO dto) {
        Pago pago = pagoRepository.findById(pagoId)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado: " + pagoId));

        TransaccionPago transaccion = new TransaccionPago();
        transaccion.setPago(pago);
        transaccion.setCodigoTransaccion(dto.getCodigoTransaccion());
        transaccion.setEstado(dto.getEstado() == null || dto.getEstado().isBlank()
                ? "PENDIENTE"
                : dto.getEstado().toUpperCase());
        transaccion.setFechaTransaccion(LocalDateTime.now());

        return convertirATransaccionDTO(transaccionPagoRepository.save(transaccion));
    }

    @Transactional
    public void eliminarTransaccion(Long id) {
        if (!transaccionPagoRepository.existsById(id)) {
            throw new RuntimeException("Transacción no encontrada: " + id);
        }
        transaccionPagoRepository.deleteById(id);
    }

    // ═══ VALIDACIÓN EXTERNA ═══════════════════════════════════════════

    private void validarPedidoExiste(Long pedidoId) {
        try {
            ResponseEntity<Object> response = restTemplate.getForEntity(
                    pedidosBaseUrl + "/" + pedidoId,
                    Object.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Pedido no encontrado: " + pedidoId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Pedido no encontrado: " + pedidoId);
        }
    }

    // ═══ CONVERSORES ══════════════════════════════════════════════════

    private PagoResponseDTO convertirAPagoDTO(Pago pago) {
        PagoResponseDTO dto = new PagoResponseDTO();
        dto.setId(pago.getId());
        dto.setMonto(pago.getMonto());
        dto.setPedidoId(pago.getPedidoId());
        dto.setEstado(pago.getEstado());
        dto.setFechaPago(pago.getFechaPago());
        dto.setMetodoPagoId(pago.getMetodoPago() != null ? pago.getMetodoPago().getId() : null);
        return dto;
    }

    private MetodoPagoResponseDTO convertirAMetodoDTO(MetodoPago metodoPago) {
        MetodoPagoResponseDTO dto = new MetodoPagoResponseDTO();
        dto.setId(metodoPago.getId());
        dto.setNombre(metodoPago.getNombre());
        dto.setActivo(metodoPago.getActivo());
        return dto;
    }

    private TransaccionPagoResponseDTO convertirATransaccionDTO(TransaccionPago transaccion) {
        TransaccionPagoResponseDTO dto = new TransaccionPagoResponseDTO();
        dto.setId(transaccion.getId());
        dto.setCodigoTransaccion(transaccion.getCodigoTransaccion());
        dto.setEstado(transaccion.getEstado());
        dto.setFechaTransaccion(transaccion.getFechaTransaccion());
        dto.setPagoId(transaccion.getPago() != null ? transaccion.getPago().getId() : null);
        return dto;
    }
}