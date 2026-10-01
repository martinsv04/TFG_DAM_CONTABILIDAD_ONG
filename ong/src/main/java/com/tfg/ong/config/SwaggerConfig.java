package com.tfg.ong.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .info(new Info()
                        .title("ONGestión - API de gestión contable para ONGs")
                        .version("1.0.0")
                        .description("Esta documentación describe los endpoints disponibles en el sistema desarrollado " +
                                "para la gestión económica y administrativa de organizaciones no gubernamentales (ONG).")
                        .contact(new Contact()
                                .name("Martín Sánchez")
                                .email("masanchezv021204@gmail.com")
                                .url("https://github.com/martinsv04"))
                );
    }
}