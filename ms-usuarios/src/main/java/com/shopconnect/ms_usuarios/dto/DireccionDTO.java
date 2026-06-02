package com.shopconnect.ms_usuarios.dto;

public class DireccionDTO {
    
    private Long id;
    private String calle;
    private String ciudad;
    private String region;
    private Long usuarioId;

    public DireccionDTO() { //Lo necesita primero para crear el objeto. Si no existe, habrá error. 
    }

    public DireccionDTO(Long id, String calle, String ciudad, String region, Long usuarioId) {
        this.id = id;
        this.calle = calle;
        this.ciudad = ciudad;
        this.region = region;
        this.usuarioId = usuarioId;
}

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

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }




}
