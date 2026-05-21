package com.shopconnect.ms_pedidos.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fechaPedido = LocalDateTime.now();

    @Column(nullable = false)
    private Double total = 0.0;

    // ID externo del microservicio ms-usuarios
    @NotNull(message = "El usuarioId es obligatorio")
    @Column(nullable = false)
    private Long usuarioId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "estado_id", nullable = false)
    private EstadoPedido estado;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();

    // Constructor vacío
    public Pedido() {}

    // Getters y Setters
    public Long getId() { 
        return id; 
    }

    public void setId(Long id) { 
        this.id = id; 
    }

    public LocalDateTime getFechaPedido() { 
        return fechaPedido; 
    }

    public void setFechaPedido(LocalDateTime fechaPedido) { 
        this.fechaPedido = fechaPedido; 
    }

    public Double getTotal() { 
        return total; 
    }

    public void setTotal(Double total) { 
        this.total = total; 
    }

    public Long getUsuarioId() { 
        return usuarioId; 
    }

    public void setUsuarioId(Long usuarioId) { 
        this.usuarioId = usuarioId; 
    }

    public EstadoPedido getEstado() { 
        return estado; 
    }

    public void setEstado(EstadoPedido estado) { 
        this.estado = estado; 
    }

    public List<DetallePedido> getDetalles() { 
        return detalles; 
    }

    public void setDetalles(List<DetallePedido> detalles) { 
        this.detalles = detalles; 
    }
}