package io.condense.config;

import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class HttpClientConfig {

    @Bean
    RestClient edgarRestClient(EdgarProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader("User-Agent", properties.userAgent())
                .requestFactory(requestFactory(properties.requestTimeout()))
                .build();
    }

    @Bean
    RestClient ollamaRestClient(OllamaProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory(properties.requestTimeout()))
                .build();
    }

    private JdkClientHttpRequestFactory requestFactory(Duration readTimeout) {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        .build());
        factory.setReadTimeout(readTimeout);
        return factory;
    }
}
