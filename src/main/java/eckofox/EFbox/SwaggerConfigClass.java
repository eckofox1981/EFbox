package eckofox.EFbox;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Configuration
@Import(SpringDocConfiguration.class)
@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "eckofox1981",
                        email = "eckofox1981@pm.me",
                        url = "https://github.com/eckofox1981"
                ),
                title = "EFBox OpenAPI documentation",
                description = "Documentation for the EFBox API. There are three controllers: User, EFBoxFolder and EFBox" +
                        "File. This application is non-cacheable.",
                version = "webservices"
        ),
        security = {
                @SecurityRequirement(name = "bearer-token"),
                @SecurityRequirement(name = "OpenIDConnect")
        }
)
@SecuritySchemes({
        @SecurityScheme(
                name = "bearer-token",
                description = "JWTToken from the API itself",
                scheme = "bearer",
                type = SecuritySchemeType.HTTP,
                bearerFormat = "JWT"
        ),
        @SecurityScheme(
                name = "OpenIDConnect",
                description = "OpenIDConnect JSESSION token (github and google as of March 2025)",
                scheme = "oauth2",
                type = SecuritySchemeType.OAUTH2,
                bearerFormat = "OAuth2"
        )
})
public class SwaggerConfigClass {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearerToken",
                                new io.swagger.v3.oas.models.security.SecurityScheme()
                                        .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                        .addSecuritySchemes("openIDConnect",
                                new io.swagger.v3.oas.models.security.SecurityScheme()
                                        .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.OAUTH2)
                                        .scheme("oauth2")
                                        .bearerFormat("OAuth2")
                        ));
    }

}
