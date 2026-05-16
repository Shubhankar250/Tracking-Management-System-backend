package com.trackingpath.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String secret;
    private long expirationSeconds;

    public String secret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long expirationSeconds() {
        return expirationSeconds;
    }

    public void setExpirationSeconds(long expirationSeconds) {
        this.expirationSeconds = expirationSeconds;
    }
}
