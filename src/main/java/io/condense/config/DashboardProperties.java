package io.condense.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "condense.dashboard")
public record DashboardProperties(int filingHistoryLimit) {
}
