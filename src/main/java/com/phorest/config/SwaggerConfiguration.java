package com.phorest.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

/** Configures Swagger. */
@Configuration
@OpenAPIDefinition(
    info =
        @Info(
            title = "${spring.application.name}",
            description = "${spring.application.description}",
            version = "${version.number}",
            contact = @Contact(name = "Filip Marinov", email = "fp.marinov@gmail.com")))
public class SwaggerConfiguration {}
