package com.codegym.bds_backend.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Security security, Cors cors, Mail mail, Cloudinary cloudinary, BootstrapAdmin bootstrapAdmin) {
    public record Security(int maxFailedLogins, int setPasswordTokenHours, int resetPasswordTokenMinutes, Jwt jwt) {
    }

    public record Jwt(String secret, long expirationMinutes) {
    }

    public record Cors(List<String> allowedOrigins) {
    }

    public record Mail(String from, String frontendUrl) {
    }

    public record Cloudinary(String cloudName, String apiKey, String apiSecret) {
    }

    public record BootstrapAdmin(String tenDangNhap, String hoTen, String email, String soDienThoai) {
    }
}
