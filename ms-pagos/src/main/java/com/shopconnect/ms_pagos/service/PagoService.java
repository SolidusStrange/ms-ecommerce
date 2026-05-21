package com.shopconnect.ms_pagos.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopconnect.ms_pagos.model.MetodoPago;
import com.shopconnect.ms_pagos.model.Pago;
import com.shopconnect.ms_pagos.model.TransaccionPago;
import com.shopconnect.ms_pagos.repository.MetodoPagoRepository;
import com.shopconnect.ms_pagos.repository.PagoRepository;
import com.shopconnect.ms_pagos.repository.TransaccionPagoRepository;

/**
 * SERVICIO: PagoService
 *
 * RESPONSABILIDAD: Lógica de negocio.
 * El Service llama a los métodos del Repository.
 * El Service NO escribe SQL ni usa EntityManager directamente.
 */
@Service
public class PagoService {

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private MetodoPagoRepository metodoPagoRepository;

    @Autowired
    private TransaccionPagoRepository transaccionPagoRepository;

    // ═══ PAGOS ════════════════════════════════════════════════════

    public List<Pago> listarTodos() {
        return pagoRepository.findAll();
    }

    public Optional<Pago> buscarPorId(Long id) {
        return pagoRepository.findById(id);
    }

    public List<Pago> listarPorPedido(Long pedidoId) {
        return pagoRepository.findByPedidoId(pedidoId);
    }

    public List<Pago> listarPorEstado(String estado) {
        return pagoRepository.findByEstado(estado.toUpperCase());
    }

    @Transactional
    public Pago procesar(Pago pago, Long metodoId) {

        // Cargar método de pago
        MetodoPago metodoPago = metodoPagoRepository.findById(metodoId)
                .orElseThrow(() -> new RuntimeException("Método de pago no encontrado: " + metodoId));

        // Validar que el método esté activo
        if (metodoPago.getActivo() != null && !metodoPago.getActivo()) {
            throw new IllegalStateException("El método de pago no está activo");
        }

        // Asignar método de pago
        pago.setMetodoPago(metodoPago);

        // Estado inicial del pago
        if (pago.getEstado() == null || pago.getEstado().isBlank()) {
            pago.setEstado("PENDIENTE");
        } else {
            pago.setEstado(pago.getEstado().toUpperCase());
        }

        // Fecha del pago
        if (pago.getFechaPago() == null) {
            pago.setFechaPago(LocalDateTime.now());
        }

        // Guardar pago primero para obtener ID
        Pago pagoGuardado = pagoRepository.save(pago);

        // Crear transacción inicial
        TransaccionPago transaccion = new TransaccionPago();
        transaccion.setPago(pagoGuardado);
        transaccion.setEstado("PENDIENTE");
        transaccion.setFechaTransaccion(LocalDateTime.now());
        transaccion.setCodigoTransaccion("TX-" + pagoGuardado.getId() + "-" + System.currentTimeMillis());

        transaccionPagoRepository.save(transaccion);

        return pagoGuardado;
    }

    @Transactional
    public Pago actualizar(Long id, Pago datos) {

        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado: " + id));

        if (datos.getMonto() != null) {
            pago.setMonto(datos.getMonto());
        }

        if (datos.getPedidoId() != null) {
            pago.setPedidoId(datos.getPedidoId());
        }

        if (datos.getFechaPago() != null) {
            pago.setFechaPago(datos.getFechaPago());
        }

        if (datos.getEstado() != null) {
            pago.setEstado(datos.getEstado().toUpperCase());
        }

        return pagoRepository.save(pago);
    }

    @Transactional
    public void eliminar(Long id) {

        if (!pagoRepository.existsById(id)) {
            throw new RuntimeException("Pago no encontrado: " + id);
        }

        pagoRepository.deleteById(id);
    }

    // ═══ MÉTODOS DE PAGO ═════════════════════════════════════════════

    public List<MetodoPago> listarMetodos() {
        return metodoPagoRepository.findAll();
    }

    public List<MetodoPago> listarMetodosActivos() {
        return metodoPagoRepository.findByActivo(true);
    }

    public Optional<MetodoPago> buscarMetodoPorId(Long id) {
        return metodoPagoRepository.findById(id);
    }

    @Transactional
    public MetodoPago crearMetodo(MetodoPago metodoPago) {

        if (metodoPagoRepository.existsByNombre(metodoPago.getNombre())) {
            throw new IllegalArgumentException("Nombre de método de pago duplicado: " + metodoPago.getNombre());
        }

        if (metodoPago.getActivo() == null) {
            metodoPago.setActivo(true);
        }

        return metodoPagoRepository.save(metodoPago);
    }

    @Transactional
    public MetodoPago actualizarMetodo(Long id, MetodoPago datos) {

        MetodoPago metodoPago = metodoPagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Método de pago no encontrado: " + id));

        if (datos.getNombre() != null) {
            if (!datos.getNombre().equals(metodoPago.getNombre())
                    && metodoPagoRepository.existsByNombre(datos.getNombre())) {
                throw new IllegalArgumentException("Nombre de método de pago duplicado: " + datos.getNombre());
            }

            metodoPago.setNombre(datos.getNombre());
        }

        if (datos.getActivo() != null) {
            metodoPago.setActivo(datos.getActivo());
        }

        return metodoPagoRepository.save(metodoPago);
    }

    @Transactional
    public void eliminarMetodo(Long id) {

        if (!metodoPagoRepository.existsById(id)) {
            throw new RuntimeException("Método de pago no encontrado: " + id);
        }

        metodoPagoRepository.deleteById(id);
    }

    // ═══ TRANSACCIONES DE PAGO ═══════════════════════════════════════

    public List<TransaccionPago> listarTransaccionesPorPago(Long pagoId) {
        return transaccionPagoRepository.findByPagoId(pagoId);
    }

    @Transactional
    public TransaccionPago agregarTransaccion(Long pagoId, TransaccionPago transaccion) {

        Pago pago = pagoRepository.findById(pagoId)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado: " + pagoId));

        transaccion.setPago(pago);

        if (transaccion.getEstado() == null || transaccion.getEstado().isBlank()) {
            transaccion.setEstado("PENDIENTE");
        } else {
            transaccion.setEstado(transaccion.getEstado().toUpperCase());
        }

        if (transaccion.getFechaTransaccion() == null) {
            transaccion.setFechaTransaccion(LocalDateTime.now());
        }

        return transaccionPagoRepository.save(transaccion);
    }

    @Transactional
    public void eliminarTransaccion(Long id) {

        if (!transaccionPagoRepository.existsById(id)) {
            throw new RuntimeException("Transacción no encontrada: " + id);
        }

        transaccionPagoRepository.deleteById(id);
    }

    // ═══ OPERACIONES ESPECÍFICAS ═══════════════════════════════════════

    @Transactional
    public Pago actualizarEstado(Long pagoId, String estado) {

        Pago pago = pagoRepository.findById(pagoId)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado: " + pagoId));

        if (estado == null || estado.isBlank()) {
            throw new IllegalArgumentException("El estado es obligatorio");
        }

        pago.setEstado(estado.toUpperCase());

        return pagoRepository.save(pago);
    }
}