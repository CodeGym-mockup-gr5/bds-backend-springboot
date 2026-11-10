package com.codegym.bds_backend.auth;

import com.codegym.bds_backend.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

/** Tạo và đọc JWT. Khóa và thời hạn lấy từ application.yml (app.security.jwt). */
@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMillis;

    public JwtService(AppProperties props) {
        byte[] secret = props.security().jwt().secret().getBytes(StandardCharsets.UTF_8);
        if (secret.length < 32) {
            throw new IllegalStateException("JWT_SECRET phải dài ít nhất 32 ký tự");
        }
        this.key = Keys.hmacShaKeyFor(secret);
        this.expirationMillis =
                Duration.ofMinutes(props.security().jwt().expirationMinutes()).toMillis();
    }

    public String generateToken(JwtPrincipal principal) {
        Date now = new Date();
        return Jwts.builder()
                .subject(principal.username())
                .claim("uid", principal.userId())
                .claim("roles", principal.roles())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMillis))
                .signWith(key)
                .compact();
    }

    /** Trả rỗng nếu token sai, hết hạn hoặc bị sửa. */
    public Optional<JwtPrincipal> parse(String token) {
        try {
            Claims claims =
                    Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            List<?> rawRoles = claims.get("roles", List.class);
            List<String> roles =
                    rawRoles == null ? List.of() : rawRoles.stream().map(String::valueOf).toList();
            Number uid = claims.get("uid", Number.class);
            return Optional.of(new JwtPrincipal(uid == null ? null : uid.longValue(), claims.getSubject(), roles));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
