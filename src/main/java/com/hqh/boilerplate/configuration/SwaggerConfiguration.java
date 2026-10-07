package com.hqh.boilerplate.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class SwaggerConfiguration {

    @Bean
    public OpenAPI quizOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Quiz App")
                        .description("API of Quiz")
                        .version("1.0")
                        .contact(new Contact()
                                .name("Pixel"))
                        .license(new License().name("MIT").url("https://justrocket.de")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    @Bean
    public OpenApiCustomizer simpleExamples() {
        Map<String, Object> examples = Map.of(
                "firstName", "Admin",
                "lastName", "User",
                "username", "adminuser",
                "currentUsername", "adminuser",
                "email", "admin@test.local",
                "password", "quiz1234",
                "roles", "ROLE_ADMIN",
                "isActive", true,
                "isNonLocked", true
        );
        return openApi -> {
            if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
                return;
            }
            openApi.getComponents().getSchemas().values().forEach(schema -> {
                if (schema.getProperties() == null) {
                    return;
                }
                schema.getProperties().forEach((name, property) -> {
                    if (!(property instanceof Schema<?> field)) {
                        return;
                    }
                    field.setPattern(null);
                    field.setMinLength(null);
                    field.setMaxLength(null);
                    if (examples.containsKey(name)) {
                        field.setExample(examples.get(name));
                    }
                });
            });
        };
    }

}
