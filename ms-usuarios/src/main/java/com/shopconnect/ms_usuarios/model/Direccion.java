package com.shopconnect.ms_usuarios.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "direccion")
public class Direccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La calle es obligatoria")
    @Column(nullable = false, length = 150)
    private String calle;

    @NotBlank(message = "La ciudad es obligatoria")
    @Column(nullable = false, length = 100)
    private String ciudad;

    @NotBlank(message = "La región es obligatoria")
    @Column(nullable = false, length = 100)
    private String region;

    // Relación con Usuario
    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // Constructor vacío
    public Direccion() {}

    // Getters y Setters
    public Long getId() { 
        return id; 
    }

    public void setId(Long id) { 
        this.id = id; 
    }

    public String getCalle() { 
        return calle; 
    }

    public void setCalle(String calle) { 
        this.calle = calle; 
    }

    public String getCiudad() { 
        return ciudad; 
    }

    public void setCiudad(String ciudad) { 
        this.ciudad = ciudad; 
    }

    public String getRegion() { 
        return region; 
    }

    public void setRegion(String region) { 
        this.region = region; 
    }

    public Usuario getUsuario() { 
        return usuario; 
    }

    public void setUsuario(Usuario usuario) { 
        this.usuario = usuario; 
    }
}