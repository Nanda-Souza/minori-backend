package com.db.minori_backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Minori API")
                        .version("1.0.0")
                        .description(
                                "API REST para gerenciamento de produtos, " +
                                        "catálogos, marcas e categorias da loja Minori."
                        )
                        .contact(new Contact()
                                .name("Equipe Minori")
                        )
                );
    }
}