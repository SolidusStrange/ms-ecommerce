package com.shopconnect.ms_pedidos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "Microservicio de Pedidos",
                version = "1.0.0",
                description = "API REST para administrar pedidos, estados y detalles.",
                contact = @Contact(name = "Equipo ShopConnect", email = "soporte@duoc.cl")
        ),
        servers = @Server(
                url = "http://localhost:8080",
                description = "Ambiente local de ms-pedidos"
        )
)
public class MsPedidosApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsPedidosApplication.class, args);
    }
}