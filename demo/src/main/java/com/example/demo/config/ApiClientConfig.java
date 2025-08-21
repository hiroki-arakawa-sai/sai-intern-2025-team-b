package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@Configuration
public class ApiClientConfig {
    @Bean
    RestClient externalApiClient(
            RestClient.Builder builder,
            @Value("${external.api.base-url}") String baseUrl,
            @Value("${external.api.key:}") String apiKey
    ) {
        return builder
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.ACCEPT, "application/json")
            .defaultHeader(HttpHeaders.AUTHORIZATION, apiKey.isBlank() ? "" : "Bearer " + apiKey)
            .build();
    }
}
