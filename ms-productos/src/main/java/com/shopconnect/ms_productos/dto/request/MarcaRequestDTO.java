package com.shopconnect.ms_productos.dto.request;

import jakarta.validation.constraints.NotBlank;

public class MarcaRequestDTO {

    @NotBlank(message = "El nombre de la marca no puede estar vacio")
    private String nombre;

    private String paisOrigen;

    private String logoUrl;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPaisOrigen() {
        return paisOrigen;
    }

    public void setPaisOrigen(String paisOrigen) {
        this.paisOrigen = paisOrigen;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }
}

