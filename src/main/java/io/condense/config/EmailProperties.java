package io.condense.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "condense.email")
public record EmailProperties(String provider) {

    public boolean isDevInbox() {
        return "dev-inbox".equals(provider);
    }
}
