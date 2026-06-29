package com.shopconnect.ms_inventario.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/*  Clase de configuracion del spring. Crea un objeto @Bean para poder inyectarlo en service 
y realizar llamadas http a otros microservicios
*/

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}