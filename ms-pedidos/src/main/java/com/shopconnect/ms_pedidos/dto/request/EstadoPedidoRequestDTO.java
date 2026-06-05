package com.shopconnect.ms_pedidos.dto.request;

import jakarta.validation.constraints.NotBlank;

public class EstadoPedidoRequestDTO {

    @NotBlank(message = "El nombre del estado es obligatorio")
    private String nombre;

    private String descripcion;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}

