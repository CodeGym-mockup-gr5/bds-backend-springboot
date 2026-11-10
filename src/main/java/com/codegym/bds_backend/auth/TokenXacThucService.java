package com.codegym.bds_backend.auth;

import com.codegym.bds_backend.common.exception.BusinessException;
import com.codegym.bds_backend.config.AppProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TokenXacThucService {
    private static final int TOKEN_BYTES = 32;
    private static final String MSG_INVALID_TOKEN = "Liên kết không hợp lệ hoặc đã hết hạn";

    private final TokenXacThucRepository tokenXacThucRepository;
    private final AppProperties props;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public String createToken(TaiKhoan taiKhoan, LoaiToken loai) {
        LocalDateTime now = LocalDateTime.now();
        tokenXacThucRepository.markDaDungByTaiKhoanId(taiKhoan.getId(), now);
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        TokenXacThuc token = new TokenXacThuc();
        token.setTaiKhoan(taiKhoan);
        token.setTokenHash(hashToken(rawToken));
        token.setLoai(loai);
        token.setHetHanLuc(now.plus(timeToLive(loai)));
        tokenXacThucRepository.save(token);
        return rawToken;
    }

    @Transactional
    public TaiKhoan consumeToken(String rawToken) {
        TokenXacThuc token = tokenXacThucRepository
                .findByTokenHash(hashToken(rawToken))
                .orElseThrow(TokenXacThucService::invalidToken);
        LocalDateTime now = LocalDateTime.now();
        if (token.getDaDungLuc() != null || token.getHetHanLuc().isBefore(now)) {
            throw invalidToken();
        }
        token.setDaDungLuc(now);
        return token.getTaiKhoan();
    }

    public static String hashToken(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Môi trường không hỗ trợ SHA-256", e);
        }
    }

    private Duration timeToLive(LoaiToken loai) {
        AppProperties.Security security = props.security();
        return loai == LoaiToken.THIET_LAP_MAT_KHAU
                ? Duration.ofHours(security.setPasswordTokenHours())
                : Duration.ofMinutes(security.resetPasswordTokenMinutes());
    }

    private static BusinessException invalidToken() {
        return new BusinessException(HttpStatus.BAD_REQUEST, MSG_INVALID_TOKEN);
    }
}
