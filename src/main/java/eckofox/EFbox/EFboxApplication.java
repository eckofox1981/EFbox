package eckofox.EFbox;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
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
				@SecurityRequirement(
						name = "bearer token"
				),
				@SecurityRequirement(
						name = "OpenIDConnect"
				)
		}
)
@SecuritySchemes ({
	@SecurityScheme(
			name = "bearer token",
			description = "JWTToken from the API itself",
			scheme = "bearer",
			type = SecuritySchemeType.HTTP,
			bearerFormat = "JWT",
			in = SecuritySchemeIn.COOKIE
),
	@SecurityScheme(
			name = "OpenIDConnect",
			description = "OpenIDConnect JSESSION token (github only as of March 2025)",
			scheme = "JSESSION",
			type = SecuritySchemeType.OAUTH2,
			bearerFormat = "OAuth2",
			in = SecuritySchemeIn.COOKIE
	)
})
public class EFboxApplication {
	public static void main(String[] args) {
		SpringApplication.run(EFboxApplication.class, args);
	}

}
