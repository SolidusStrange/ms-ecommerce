package com.shopconnect.ms_usuarios.dto;

public class UsuarioDTO {

    private Long id;
    private String nombre;
    private String email;
    private Boolean activo;
    private Long rolId;

    public UsuarioDTO() {
    }

    public UsuarioDTO(Long id, String nombre, Boolean activo, String email,   Long rolId) {
        this.id = id;
        this.nombre = nombre;
        this.activo = activo;
        this.email = email;
        this.rolId = rolId;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Long getRolId() {
        return rolId;
    }

    public void setRolId(Long rolId) {
        this.rolId = rolId;
    }

    

}
