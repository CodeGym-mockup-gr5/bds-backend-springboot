package com.codegym.bds_backend.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.codegym.bds_backend.config.AppProperties;
import java.util.List;
import org.junit.jupiter.api.Test;

class JwtServiceTest {
    private static final String SECRET = "test-secret-test-secret-test-secret-1234";

    private static AppProperties props(String secret, long minutes) {
        return new AppProperties(
                new AppProperties.Security(5, 24, 30, new AppProperties.Jwt(secret, minutes)),
                new AppProperties.Cors(List.of("http://localhost:5173")),
                new AppProperties.Mail("no-reply@test.local", "http://localhost:5173"),
                new AppProperties.Cloudinary("", "", ""),
                new AppProperties.BootstrapAdmin("admin", "Quản trị viên", "admin@test.local", "0900000000"));
    }

    @Test
    void shouldReadBackWhatWasIssued() {
        JwtService service = new JwtService(props(SECRET, 60));
        String token = service.generateToken(new JwtPrincipal(7L, "KH007", List.of("ROLE_KHACH_HANG")));

        JwtPrincipal result = service.parse(token).orElseThrow();

        assertEquals(7L, result.userId());
        assertEquals("KH007", result.username());
        assertEquals(List.of("ROLE_KHACH_HANG"), result.roles());
    }

    @Test
    void shouldRejectTamperedToken() {
        JwtService service = new JwtService(props(SECRET, 60));
        String token = service.generateToken(new JwtPrincipal(1L, "admin", List.of("ROLE_ADMIN")));

        assertTrue(service.parse(token + "x").isEmpty());
        assertTrue(service.parse("khong-phai-token").isEmpty());
    }

    @Test
    void shouldRejectExpiredToken() {
        JwtService service = new JwtService(props(SECRET, -1));
        String token = service.generateToken(new JwtPrincipal(1L, "admin", List.of("ROLE_ADMIN")));

        assertTrue(service.parse(token).isEmpty());
    }

    @Test
    void shouldRejectShortSecret() {
        assertThrows(IllegalStateException.class, () -> new JwtService(props("ngan", 60)));
    }
}
