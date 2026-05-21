package com.shopconnect.ms_inventario.model;

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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "movimiento_inventario")
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Valores esperados:
     * ENTRADA -> aumenta stock
     * SALIDA  -> disminuye stock
     */
    @NotBlank(message = "El tipo de movimiento es obligatorio")
    @Column(nullable = false, length = 50)
    private String tipo;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false)
    private LocalDateTime fechaMovimiento = LocalDateTime.now();

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "inventario_id", nullable = false)
    private Inventario inventario;

    // Constructor vacío
    public MovimientoInventario() {}

    // Getters y Setters
    public Long getId() { 
        return id; 
    }

    public void setId(Long id) { 
        this.id = id; 
    }

    public String getTipo() { 
        return tipo; 
    }

    public void setTipo(String tipo) { 
        this.tipo = tipo; 
    }

    public Integer getCantidad() { 
        return cantidad; 
    }

    public void setCantidad(Integer cantidad) { 
        this.cantidad = cantidad; 
    }

    public LocalDateTime getFechaMovimiento() { 
        return fechaMovimiento; 
    }

    public void setFechaMovimiento(LocalDateTime fechaMovimiento) { 
        this.fechaMovimiento = fechaMovimiento; 
    }

    public Inventario getInventario() { 
        return inventario; 
    }

    public void setInventario(Inventario inventario) { 
        this.inventario = inventario; 
    }
}