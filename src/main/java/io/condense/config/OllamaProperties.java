package io.condense.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "condense.ollama")
public record OllamaProperties(
        String baseUrl,
        String model,
        Duration requestTimeout,
        double temperature,
        int contextWindowTokens) {
}
