package gym.ada.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        return new OpenAPI()

                .addServersItem(
                        new Server().url("/")
                )

                .components(
                        new Components()
                                .addSecuritySchemes(
                                        "firebaseAuth",
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                                .description(
                                                        "Ingresar Firebase ID Token"
                                                )
                                )
                )

                .addSecurityItem(
                        new SecurityRequirement()
                                .addList("firebaseAuth")
                );
    }
}