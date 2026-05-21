package com.shopconnect.ms_pagos.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "pago")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El monto es obligatorio")
    @Min(value = 0, message = "El monto no puede ser negativo")
    @Column(nullable = false)
    private Double monto;

    // ID externo del microservicio ms-pedidos
    @NotNull(message = "El pedidoId es obligatorio")
    @Column(nullable = false)
    private Long pedidoId;

    @Column(nullable = false, length = 50)
    private String estado = "PENDIENTE";

    @Column(nullable = false)
    private LocalDateTime fechaPago = LocalDateTime.now();

    @ManyToOne(optional = false)
    @JoinColumn(name = "metodo_pago_id", nullable = false)
    private MetodoPago metodoPago;

    // Constructor vacío
    public Pago() {}

    // Getters y Setters
    public Long getId() { 
        return id; 
    }

    public void setId(Long id) { 
        this.id = id; 
    }

    public Double getMonto() { 
        return monto; 
    }

    public void setMonto(Double monto) { 
        this.monto = monto; 
    }

    public Long getPedidoId() { 
        return pedidoId; 
    }

    public void setPedidoId(Long pedidoId) { 
        this.pedidoId = pedidoId; 
    }

    public String getEstado() { 
        return estado; 
    }

    public void setEstado(String estado) { 
        this.estado = estado; 
    }

    public LocalDateTime getFechaPago() { 
        return fechaPago; 
    }

    public void setFechaPago(LocalDateTime fechaPago) { 
        this.fechaPago = fechaPago; 
    }

    public MetodoPago getMetodoPago() { 
        return metodoPago; 
    }

    public void setMetodoPago(MetodoPago metodoPago) { 
        this.metodoPago = metodoPago; 
    }
}