package com.taskflow.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI taskflowOpenAPI() {
        final String schemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("TaskFlow API")
                        .description("""
                                Task management REST API with **JWT authentication** and \
                                **role-based access control** (`USER` owns their tasks; `ADMIN` sees all).

                                **Try it:** register → login → authorize with the Bearer token → create tasks. \
                                An Angular 19 SPA in `frontend/` consumes the same endpoints.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Kola Mohan Venkatesh")
                                .url("https://github.com/Mohangab"))
                        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local (Maven / Docker)")))
                .addSecurityItem(new SecurityRequirement().addList(schemeName))
                .components(new Components()
                        .addSecuritySchemes(schemeName, new SecurityScheme()
                                .name(schemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste the accessToken from POST /api/auth/login")));
    }
}
