package com.sonu.candidatehub.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI candidateHubOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("CandidateHub API")
                .description("REST API for managing candidate information — create, search, filter, update, and delete candidates.")
                .version("v1")
                .contact(new Contact().name("Sonu Sankhala")));
    }
}