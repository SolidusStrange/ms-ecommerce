package com.shopconnect.ms_productos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;


@Entity // Le indicamos a Hibernate que es una tabla
@Table(name = "imagenProducto") // Le asignamos un nombre en la tabla
public class ImagenProducto {

    // Atributos con anotaciones JPA

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "No puede estar vacio")
    @Column(nullable= false)
    private String url;

    private Integer orden = 0;
    private Boolean principal = false;

    @ManyToOne(optional = false)
    @JoinColumn(name = "producto_id")
    private Producto producto;



    // --- Constructor vacio  --------------------------------
    public ImagenProducto() {}


    // --- Getters y Setters  --------------------------------
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }

    public Boolean getPrincipal() { return principal; }
    public void setPrincipal(Boolean principal) { this.principal = principal; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }


    
}
