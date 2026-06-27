package com.shopconnect.ms_inventario;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "Microservicio de Inventario",
                version = "1.0.0",
                description = "API REST para administrar inventarios y movimientos de stock.",
                contact = @Contact(name = "Equipo DSY1103", email = "soporte@duoc.cl")
        ),
        servers = @Server(
                url = "http://localhost:8080",
                description = "Ambiente local de ms-inventario"
        )
)
public class MsInventarioApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsInventarioApplication.class, args);
    }
}

