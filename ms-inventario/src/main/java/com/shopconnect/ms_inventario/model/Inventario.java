package com.shopconnect.ms_inventario.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "inventario")
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ID externo del microservicio ms-productos
    @NotNull(message = "El productoId es obligatorio")
    @Column(nullable = false, unique = true)
    private Long productoId;

    @NotNull(message = "El stock actual es obligatorio")
    @Min(value = 0, message = "El stock actual no puede ser negativo")
    @Column(nullable = false)
    private Integer stockActual = 0;

    @NotNull(message = "El stock mínimo es obligatorio")
    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    @Column(nullable = false)
    private Integer stockMinimo = 0;

    // Constructor vacío
    public Inventario() {}

    // Getters y Setters
    public Long getId() { 
        return id; 
    }

    public void setId(Long id) { 
        this.id = id; 
    }

    public Long getProductoId() { 
        return productoId; 
    }

    public void setProductoId(Long productoId) { 
        this.productoId = productoId; 
    }

    public Integer getStockActual() { 
        return stockActual; 
    }

    public void setStockActual(Integer stockActual) { 
        this.stockActual = stockActual; 
    }

    public Integer getStockMinimo() { 
        return stockMinimo; 
    }

    public void setStockMinimo(Integer stockMinimo) { 
        this.stockMinimo = stockMinimo; 
    }
}