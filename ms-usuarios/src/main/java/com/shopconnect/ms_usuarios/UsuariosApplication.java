package com.shopconnect.ms_usuarios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "Microservicio de Usuarios",
                version = "1.0.0",
                description = "API REST para administrar usuarios, roles y direcciones.",
                contact = @Contact(
                        name = "Equipo ShopConnect",
                        email = "soporte@duoc.cl"
                )
        ),
        servers = @Server(
                url = "http://localhost:8080",
                description = "Ambiente local de ms-usuarios"
        )
)
public class UsuariosApplication {

    public static void main(String[] args) {
        SpringApplication.run(UsuariosApplication.class, args);
    }
}