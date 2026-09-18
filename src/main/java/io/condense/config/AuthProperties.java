package io.condense.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "condense.auth")
public record AuthProperties(Duration linkLifetime) {
}
