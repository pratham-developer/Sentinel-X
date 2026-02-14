package com.pratham.sentinelx.config;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.concurrent.TimeUnit;

@Configuration
public class RestClientConfig {

    @Value("${ai.server.url}")
    private String aiServerUrl;

    @Value("${ai.server.secret}")
    private String aiServerSecret;

    @Bean
    public RestClient pythonClient(RestClient.Builder builder) {
        // connection pooling
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(200); // handle 200 concurrent calls
        connectionManager.setDefaultMaxPerRoute(50); // max 50 to python ai

        // timeouts (dont let the ai hang the user)
        RequestConfig requestConfig = RequestConfig.custom()
                .setResponseTimeout(Timeout.of(10, TimeUnit.SECONDS)) // hard limit: 2s
                .build();

        HttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();

        return builder
                .requestFactory(new HttpComponentsClientHttpRequestFactory(httpClient))
                .baseUrl(aiServerUrl) // Python Server
                .defaultHeader("Accept-Encoding", "gzip") // critical for speed
                .defaultHeader("X-Internal-Secret", aiServerSecret) // security
                .build();
    }

}
