package com.codegym.bds_backend.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codegym.bds_backend.common.exception.BusinessException;
import com.codegym.bds_backend.config.AppProperties;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class TokenXacThucServiceTest {
    private static final String RAW = "abc";
    private static final String SHA256_OF_ABC = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad";

    @Mock
    private TokenXacThucRepository tokenXacThucRepository;

    private TokenXacThucService service;

    @BeforeEach
    void setUp() {
        AppProperties props = new AppProperties(
                new AppProperties.Security(5, 24, 30, new AppProperties.Jwt("x", 60)),
                new AppProperties.Cors(List.of()),
                new AppProperties.Mail("no-reply@test.local", "http://localhost:5173"),
                new AppProperties.Cloudinary("", "", ""),
                new AppProperties.BootstrapAdmin("admin", "Quản trị viên", "admin@test.local", "0900000000"));
        service = new TokenXacThucService(tokenXacThucRepository, props);
    }

    @Test
    void shouldHashWithSha256() {
        assertEquals(SHA256_OF_ABC, TokenXacThucService.hashToken(RAW));
    }

    @Test
    void shouldStoreOnlyHashOfIssuedToken() {
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setId(5L);
        when(tokenXacThucRepository.save(any(TokenXacThuc.class))).thenAnswer(call -> call.getArgument(0));

        String raw = service.createToken(taiKhoan, LoaiToken.THIET_LAP_MAT_KHAU);

        ArgumentCaptor<TokenXacThuc> captor = ArgumentCaptor.forClass(TokenXacThuc.class);
        verify(tokenXacThucRepository).save(captor.capture());
        TokenXacThuc saved = captor.getValue();
        assertNotEquals(raw, saved.getTokenHash());
        assertEquals(TokenXacThucService.hashToken(raw), saved.getTokenHash());
        assertEquals(64, saved.getTokenHash().length());
        assertTrue(saved.getHetHanLuc().isAfter(LocalDateTime.now().plusHours(23)));
    }

    @Test
    void shouldRejectExpiredToken() {
        TokenXacThuc token = token(LocalDateTime.now().minusMinutes(1), null);
        when(tokenXacThucRepository.findByTokenHash(SHA256_OF_ABC)).thenReturn(Optional.of(token));

        BusinessException e = assertThrows(BusinessException.class, () -> service.consumeToken(RAW));

        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
    }

    @Test
    void shouldRejectUsedToken() {
        TokenXacThuc token = token(LocalDateTime.now().plusMinutes(10), LocalDateTime.now().minusMinutes(1));
        when(tokenXacThucRepository.findByTokenHash(SHA256_OF_ABC)).thenReturn(Optional.of(token));

        assertThrows(BusinessException.class, () -> service.consumeToken(RAW));
    }

    @Test
    void shouldRejectUnknownToken() {
        when(tokenXacThucRepository.findByTokenHash(SHA256_OF_ABC)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> service.consumeToken(RAW));
    }

    @Test
    void shouldMarkTokenUsedWhenValid() {
        TokenXacThuc token = token(LocalDateTime.now().plusMinutes(10), null);
        when(tokenXacThucRepository.findByTokenHash(SHA256_OF_ABC)).thenReturn(Optional.of(token));

        TaiKhoan result = service.consumeToken(RAW);

        assertSame(token.getTaiKhoan(), result);
        assertNotNull(token.getDaDungLuc());
    }

    private static TokenXacThuc token(LocalDateTime hetHanLuc, LocalDateTime daDungLuc) {
        TokenXacThuc token = new TokenXacThuc();
        token.setTaiKhoan(new TaiKhoan());
        token.setHetHanLuc(hetHanLuc);
        token.setDaDungLuc(daDungLuc);
        return token;
    }
}
