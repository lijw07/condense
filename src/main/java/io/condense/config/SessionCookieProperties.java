package io.condense.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "server.servlet.session.cookie")
public record SessionCookieProperties(String name) {
}
