package com.shopconnect.ms_productos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity // Le indicamos a Hibernate que es una tabla
@Table(name = "categoria") // Le asignamos el nombre a la tabla
public class Categoria {

    // Atributos con anotaciones JPA

    @Id // Indicamos que es la PK
    @GeneratedValue(strategy= GenerationType.IDENTITY) // Le damos un valor autoincrementable
    private Long id;

    @NotBlank(message = "El nombre de la categoria es obligatoria") // Mensaje por si no se ingresa nada
    @Column(nullable= false, length= 100) // La columna no puede estar vacia y el límite máximo son 100
    private String nombre; // "electrónica, ropa, computadores"

    private String descripcion; // "Lenovo SSD500, 4GB RAM"

    @Column(nullable= false)
    private Boolean activa = true;

    // --- Relación con  --------------------------------



    // --- Constructor vacio  --------------------------------
        public Categoria() {}

    // -- Getters y Setters ----------------------------------
        public Long getId() { return id;  }
        public void setId(Long id) { this.id = id; }

        public String getNombre() { return nombre;}
        public void setNombre(String nombre) { this.nombre = nombre;}

        public String getDescripcion() {return descripcion;}
        public void setDescripcion(String descripcion) { this.descripcion = descripcion;}

        public Boolean getActiva() {return activa;}
        public void setActiva(Boolean activa) { this.activa = activa;}


}
