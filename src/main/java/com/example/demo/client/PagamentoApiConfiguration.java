package com.example.demo.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class PagamentoApiConfiguration {

    @Bean
    public RestClient pagamentosRestClient(
            @Value("${pagamentos.api.base-url}") String baseUrl,
            @Value("${pagamentos.api.connect-timeout:1000}") long connectTimeoutMillis,
            @Value("${pagamentos.api.read-timeout:2000}") long readTimeoutMillis
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(connectTimeoutMillis));
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMillis));
        return RestClient.builder().baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }
}
