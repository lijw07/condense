package io.condense.config;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "condense.edgar")
public record EdgarProperties(
        String baseUrl,
        String userAgent,
        Duration requestTimeout,
        Duration minimumRequestInterval,
        List<String> trackedForms,
        int maxDocumentChars,
        boolean bootstrapCompanies) {
}
