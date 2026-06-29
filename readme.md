# ShopConnect - E-commerce con Microservicios

Proyecto Fullstack desarrollado con arquitectura de microservicios para simular el funcionamiento de una tienda online (e-commerce).

## Descripción

ShopConnect separa las responsabilidades del sistema en servicios independientes, permitiendo una mejor escalabilidad, mantenibilidad y desacoplamiento entre módulos.

Cada microservicio administra un dominio específico del negocio:

- **ms-productos** → Gestión del catálogo de productos, marcas y categorías.
- **ms-usuarios** → Registro y administración de clientes y perfiles.
- **ms-inventario** → Control de stock y disponibilidad de productos.
- **ms-pedidos** → Creación y seguimiento de pedidos.
- **ms-pagos** → Procesamiento y registro de pagos.

## Arquitectura

El sistema está basado en una arquitectura de microservicios donde cada servicio posee:

- Base de datos propia
- Lógica de negocio independiente
- API REST para comunicación entre servicios

Flujo general de compra:

1. El cliente consulta productos.
2. Selecciona uno o más productos.
3. El sistema verifica stock en **ms-inventario**.
4. Se genera el pedido en **ms-pedidos**.
5. Se procesa el pago en **ms-pagos**.
6. Si el pago es exitoso, se descuenta stock y se confirma la compra.

## Tecnologías utilizadas

- Java 21
- Spring Boot
- Spring Data JPA
- Hibernate
- Oracle Database
- Maven
- Docker
- Swagger / OpenAPI
- REST APIs

## Objetivo del proyecto

El objetivo es aplicar conceptos de:

- Arquitectura de microservicios
- Desarrollo backend con Spring Boot
- Persistencia con JPA/Hibernate
- Diseño de bases de datos relacionales
- Comunicación entre servicios REST

## Integrantes

- Simón González
- Andrés Mendoza
- Ignacio Arntz
- José Díaz