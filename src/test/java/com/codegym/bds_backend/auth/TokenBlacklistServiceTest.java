package com.codegym.bds_backend.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TokenBlacklistServiceTest {

    private TokenBlacklistService service;

    @BeforeEach
    void setUp() {
        service = new TokenBlacklistService();
    }

    @Test
    @DisplayName("Token chưa revoke thì isRevoked trả về false")
    void shouldReturnFalseWhenTokenNotRevoked() {
        assertFalse(service.isRevoked("token_chua_revoke"));
    }

    @Test
    @DisplayName("Token đã revoke và chưa quá hạn thì isRevoked trả về true")
    void shouldReturnTrueWhenTokenRevoked() {
        String token = "jwt_token_sample";
        Date futureDate = new Date(System.currentTimeMillis() + 60000);

        service.revokeToken(token, futureDate);

        assertTrue(service.isRevoked(token));
    }

    @Test
    @DisplayName("Token đã revoke nhưng đã quá hạn tự nhiên thì tự động dọn dẹp và trả về false")
    void shouldReturnFalseWhenRevokedTokenExpired() {
        String token = "jwt_token_expired";
        Date pastDate = new Date(System.currentTimeMillis() - 1000);

        service.revokeToken(token, pastDate);

        assertFalse(service.isRevoked(token));
    }

    @Test
    @DisplayName("Gọi cleanupExpiredTokens không gây lỗi và dọn dẹp các token hết hạn")
    void shouldCleanupExpiredTokensSuccessfully() {
        String token1 = "expired_token";
        String token2 = "active_token";
        service.revokeToken(token1, new Date(System.currentTimeMillis() - 1000));
        service.revokeToken(token2, new Date(System.currentTimeMillis() + 60000));

        service.cleanupExpiredTokens();

        assertFalse(service.isRevoked(token1));
        assertTrue(service.isRevoked(token2));
    }

    @Test
    @DisplayName("Truyền token null hoặc expiration null thì bỏ qua an toàn")
    void shouldHandleNullGracefully() {
        service.revokeToken(null, new Date());
        service.revokeToken("token", null);

        assertFalse(service.isRevoked(null));
    }
}

