package io.condense.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private final SessionCookieProperties sessionCookie;

    public OpenApiConfig(SessionCookieProperties sessionCookie) {
        this.sessionCookie = sessionCookie;
    }

    @Bean
    public OpenAPI condenseOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Condense API")
                        .description("""
                                Endpoints under /api/me require an authenticated session.

                                To get one: POST /auth/request-link with your email, open the sign-in link \
                                (captured at /dev/inbox when condense.email.provider is dev-inbox), then come back. \
                                Swagger UI sends the session cookie automatically once it is set, so there is \
                                no token to paste.

                                /api/subscriptions creates a subscriber from an email address alone; \
                                tickers may be omitted."""))
                .components(new Components().addSecuritySchemes(sessionCookie.name(), new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.COOKIE)
                        .name(sessionCookie.name())
                        .description("Session cookie minted by GET /auth/verify")));
    }
}
