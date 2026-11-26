package com.codegym.bds_backend.auth.validation;

import com.codegym.bds_backend.common.exception.BadRequestException;
import java.util.Locale;
import java.util.Set;

public final class PasswordPolicy {
    public static final int MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 64;
    public static final int MAX_CONSECUTIVE_REPEAT = 4;
    public static final int MIN_CONTEXT_TOKEN_LENGTH = 4;
    public static final String SCHEMA_DESCRIPTION =
            "Mật khẩu 8-64 ký tự, không khoảng trắng/dấu, gồm chữ thường, "
                    + "chữ hoa, số, ký tự đặc biệt, không lặp 4 ký tự liên tiếp "
                    + "và không chứa tên đăng nhập/email";

    private static final Set<String> WEAK_PASSWORDS = Set.of(
            "password1!",
            "password@1",
            "password@123",
            "password123!",
            "p@ssword1",
            "p@ssw0rd",
            "p@ssw0rd1",
            "p@ssw0rd!",
            "admin@123",
            "admin@1234",
            "admin123!",
            "admin123@",
            "qwerty@123",
            "qwerty123!",
            "qwerty1!",
            "abcd@1234",
            "abc@1234",
            "abc12345!",
            "welcome@1",
            "welcome@123",
            "welcome123!",
            "12345678a!",
            "12345678a@",
            "a1234567!",
            "a1234567@",
            "test@1234",
            "test1234!",
            "user@1234",
            "user1234!",
            "vietnam@123",
            "vietnam123!",
            "matkhau@1",
            "matkhau@123",
            "matkhau123!",
            "realia@123",
            "iloveyou1!",
            "changeme1!",
            "changeme@1",
            "letmein@1",
            "pass@1234");

    private PasswordPolicy() {
    }

    public static boolean isWeakPassword(String password) {
        return password != null && WEAK_PASSWORDS.contains(password.toLowerCase(Locale.ROOT));
    }

    public static void validateContextMatKhau(String field, String matKhau, String tenDangNhap, String email) {
        if (matKhau == null || matKhau.isBlank()) {
            return;
        }
        String lowerPass = matKhau.toLowerCase(Locale.ROOT);
        if (containsSensitiveToken(lowerPass, tenDangNhap)
                || containsSensitiveToken(lowerPass, extractEmailLocalPart(email))) {
            throw BadRequestException.fieldError(
                    field, "Mật khẩu không được chứa tên đăng nhập hoặc email");
        }
    }

    private static boolean containsSensitiveToken(String lowerPassword, String token) {
        if (token == null) {
            return false;
        }
        String normalized = token.trim().toLowerCase(Locale.ROOT);
        return normalized.length() >= MIN_CONTEXT_TOKEN_LENGTH && lowerPassword.contains(normalized);
    }

    private static String extractEmailLocalPart(String email) {
        if (email == null) {
            return null;
        }
        int atIndex = email.indexOf('@');
        return atIndex > 0 ? email.substring(0, atIndex) : null;
    }
}
