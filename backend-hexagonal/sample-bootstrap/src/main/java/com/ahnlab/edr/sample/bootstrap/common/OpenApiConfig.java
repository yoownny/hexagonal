package com.ahnlab.edr.sample.bootstrap.common;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3 전역 설정.
 * Swagger UI: /swagger-ui.html
 * API Docs:   /v3/api-docs
 */
@OpenAPIDefinition(
        info = @Info(
                title = "Sample EDR API",
                version = "v1.0",
                description = "AhnLab EDR Sample 서비스 REST API 명세서",
                contact = @Contact(
                        name = "AhnLab EDR Team",
                        email = "edr@ahnlab.com"
                ),
                license = @License(
                        name = "AhnLab Proprietary",
                        url = "https://www.ahnlab.com"
                )
        ),
        servers = {
                @Server(url = "/", description = "기본 서버")
        }
)
@Configuration
public class OpenApiConfig {
}
