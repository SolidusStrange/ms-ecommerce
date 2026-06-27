package com.shopconnect.ms_productos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "Microservicio de Productos",
                version = "1.0.0",
                description = "API REST para administrar productos, categorías, marcas e imágenes.",
                contact = @Contact(name = "Equipo ShopConnect", email = "soporte@duoc.cl")
        ),
        servers = @Server(
                url = "http://localhost:8080",
                description = "Ambiente local de ms-productos"
        )
)
public class MsProductosApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsProductosApplication.class, args);
    }
}