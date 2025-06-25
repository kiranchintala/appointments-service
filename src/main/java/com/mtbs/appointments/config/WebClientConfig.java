package com.mtbs.appointments.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${service.catalogue.base-url}")
    private String catalogueServiceBaseUrl;

    @Bean
    public WebClient catalogueServiceWebClient() {
        return WebClient.builder()
                .baseUrl(catalogueServiceBaseUrl)
                .build();
    }
}
