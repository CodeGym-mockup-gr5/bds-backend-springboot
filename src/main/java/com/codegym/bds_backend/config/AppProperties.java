package com.codegym.bds_backend.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Cấu hình của ứng dụng, đọc từ khóa app.* trong application.yml. */
@ConfigurationProperties(prefix = "app")
public record AppProperties(Security security, Cors cors, Mail mail, Cloudinary cloudinary) {

    public record Security(int maxFailedLogins, int setPasswordTokenHours, int resetPasswordTokenMinutes, Jwt jwt) {
    }

    public record Jwt(String secret, long expirationMinutes) {
    }

    public record Cors(List<String> allowedOrigins) {
    }

    public record Mail(String from) {
    }

    public record Cloudinary(String cloudName, String apiKey, String apiSecret) {
    }
}
