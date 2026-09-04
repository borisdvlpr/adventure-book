package com.pictet.adventurebook.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Adventure Book API")
                        .description("""
                                Browse a catalogue of choose-your-own-adventure books, read them \
                                section by section, and play them as a stateful game.
                                
                                Errors are RFC 9457 problem details; each failure carries its own \
                                `type` URI so a client can branch on the kind of failure without \
                                matching on the message.""")
                        .version("v1"));
    }
}