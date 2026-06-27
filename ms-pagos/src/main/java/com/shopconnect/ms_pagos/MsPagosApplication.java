package com.shopconnect.ms_pagos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "Microservicio de Pagos",
                version = "1.0.0",
                description = "API REST para administrar pagos, métodos de pago y transacciones.",
                contact = @Contact(name = "Equipo ShopConnect", email = "soporte@duoc.cl")
        ),
        servers = @Server(
                url = "http://localhost:8085",
                description = "Ambiente local de ms-pagos"
        )
)
public class MsPagosApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsPagosApplication.class, args);
    }
}