package com.shopconnect.ms_usuarios.dto;

public class RolUsuarioDTO {

    private Long id;
    private String nombre;
    private String descripcion;

    public RolUsuarioDTO() {
    }

    public RolUsuarioDTO(String descripcion, Long id, String nombre) {
        this.descripcion = descripcion;
        this.id = id;
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
