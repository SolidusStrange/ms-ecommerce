package com.shopconnect.ms_pedidos.model;

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
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "detalle_pedido")
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    @Column(nullable = false)
    private Integer cantidad;

    @NotNull(message = "El precio unitario es obligatorio")
    @Column(nullable = false)
    private Double precioUnit;

    // ID externo del microservicio ms-productos
    @NotNull(message = "El productoId es obligatorio")
    @Column(nullable = false)
    private Long productoId;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    // Constructor vacío
    public DetallePedido() {}

    // Getters y Setters
    public Long getId() { 
        return id; 
    }

    public void setId(Long id) { 
        this.id = id; 
    }

    public Integer getCantidad() { 
        return cantidad; 
    }

    public void setCantidad(Integer cantidad) { 
        this.cantidad = cantidad; 
    }

    public Double getPrecioUnit() { 
        return precioUnit; 
    }

    public void setPrecioUnit(Double precioUnit) { 
        this.precioUnit = precioUnit; 
    }

    public Long getProductoId() { 
        return productoId; 
    }

    public void setProductoId(Long productoId) { 
        this.productoId = productoId; 
    }

    public Pedido getPedido() { 
        return pedido; 
    }

    public void setPedido(Pedido pedido) { 
        this.pedido = pedido; 
    }
}