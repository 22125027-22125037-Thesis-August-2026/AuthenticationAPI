package com.mhsa.backend.dashboard.config;

import java.time.Duration;

import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.web.client.ClientHttpRequestFactorySupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .requestFactory(new ClientHttpRequestFactorySupplier()
                        .get(new ClientHttpRequestFactorySettings(Duration.ofSeconds(5))))
                .build();
    }
}
