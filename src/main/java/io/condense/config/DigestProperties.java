package io.condense.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "condense.digest")
public record DigestProperties(
        String fromAddress,
        String fromName,
        String siteUrl,
        int maxTickersPerSubscriber,
        int maxFilingsPerDigest) {
}
