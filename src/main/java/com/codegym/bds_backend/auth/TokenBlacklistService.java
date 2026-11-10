package com.codegym.bds_backend.auth;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TokenBlacklistService {

    private final Map<String, Instant> blacklist = new ConcurrentHashMap<>();

    public void revokeToken(String token, Date expiresAt) {
        if (token == null || expiresAt == null) {
            return;
        }
        blacklist.put(token, expiresAt.toInstant());
        log.info("Đã đưa token vào danh sách thu hồi (blacklist)");
    }

    public boolean isRevoked(String token) {
        if (token == null) {
            return false;
        }
        Instant expiresAt = blacklist.get(token);
        if (expiresAt == null) {
            return false;
        }
        if (Instant.now().isAfter(expiresAt)) {
            blacklist.remove(token);
            return false;
        }
        return true;
    }

    @Scheduled(fixedRate = 3600000)
    public void cleanupExpiredTokens() {
        Instant now = Instant.now();
        blacklist.entrySet().removeIf(entry -> now.isAfter(entry.getValue()));
        log.debug("Đã dọn dẹp các token hết hạn trong blacklist");
    }
}

