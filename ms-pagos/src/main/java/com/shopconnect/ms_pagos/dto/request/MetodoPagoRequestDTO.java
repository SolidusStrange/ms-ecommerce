package com.shopconnect.ms_pagos.dto.request;

import jakarta.validation.constraints.NotBlank;

public class MetodoPagoRequestDTO {

    @NotBlank(message = "El nombre del método de pago es obligatorio")
    private String nombre;

    private Boolean activo = true;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}

