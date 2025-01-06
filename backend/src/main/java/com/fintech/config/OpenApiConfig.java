package com.fintech.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI finTechOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("FinTech Portfolio API")
                .description("API for managing investment portfolios and market data")
                .version("1.0.0")
                .contact(new Contact()
                    .name("FinTech Team")
                    .email("support@fintech.com")));
    }
} 