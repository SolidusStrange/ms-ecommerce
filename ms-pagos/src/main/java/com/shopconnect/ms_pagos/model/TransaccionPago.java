package com.shopconnect.ms_pagos.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "transaccion_pago")
public class TransaccionPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El código de transacción es obligatorio")
    @Column(nullable = false, unique = true, length = 100)
    private String codigoTransaccion;

    @Column(nullable = false, length = 50)
    private String estado = "PENDIENTE";

    @Column(nullable = false)
    private LocalDateTime fechaTransaccion = LocalDateTime.now();

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "pago_id", nullable = false)
    private Pago pago;

    // Constructor vacío
    public TransaccionPago() {}

    // Getters y Setters
    public Long getId() { 
        return id; 
    }

    public void setId(Long id) { 
        this.id = id; 
    }

    public String getCodigoTransaccion() { 
        return codigoTransaccion; 
    }

    public void setCodigoTransaccion(String codigoTransaccion) { 
        this.codigoTransaccion = codigoTransaccion; 
    }

    public String getEstado() { 
        return estado; 
    }

    public void setEstado(String estado) { 
        this.estado = estado; 
    }

    public LocalDateTime getFechaTransaccion() { 
        return fechaTransaccion; 
    }

    public void setFechaTransaccion(LocalDateTime fechaTransaccion) { 
        this.fechaTransaccion = fechaTransaccion; 
    }

    public Pago getPago() { 
        return pago; 
    }

    public void setPago(Pago pago) { 
        this.pago = pago; 
    }
}