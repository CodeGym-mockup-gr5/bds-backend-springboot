package com.codegym.bds_backend.auth.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;

public class ValidPasswordValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        if (value.length() < PasswordPolicy.MIN_LENGTH || value.length() > PasswordPolicy.MAX_LENGTH) {
            return fail(context, "Mật khẩu phải từ 8 đến 64 ký tự");
        }

        boolean hasLower = false;
        boolean hasUpper = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        int repeatCount = 1;
        boolean hasMaxRepeat = false;

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isWhitespace(c)) {
                return fail(context, "Mật khẩu không được chứa khoảng trắng");
            }
            if (c < 0x21 || c > 0x7E) {
                return fail(
                        context,
                        "Mật khẩu chỉ gồm chữ không dấu, số và ký tự đặc biệt cơ bản");
            }
            if (c >= 'a' && c <= 'z') {
                hasLower = true;
            } else if (c >= 'A' && c <= 'Z') {
                hasUpper = true;
            } else if (c >= '0' && c <= '9') {
                hasDigit = true;
            } else {
                hasSpecial = true;
            }
            if (i > 0) {
                repeatCount = c == value.charAt(i - 1) ? repeatCount + 1 : 1;
                if (repeatCount >= PasswordPolicy.MAX_CONSECUTIVE_REPEAT) {
                    hasMaxRepeat = true;
                }
            }
        }

        List<String> missing = new ArrayList<>();
        if (!hasLower) {
            missing.add("chữ thường");
        }
        if (!hasUpper) {
            missing.add("chữ in hoa");
        }
        if (!hasDigit) {
            missing.add("chữ số");
        }
        if (!hasSpecial) {
            missing.add("ký tự đặc biệt");
        }
        if (!missing.isEmpty()) {
            return fail(context, "Mật khẩu cần có ít nhất: " + String.join(", ", missing));
        }
        if (hasMaxRepeat) {
            return fail(context, "Mật khẩu không được chứa 4 ký tự giống nhau liên tiếp");
        }
        if (PasswordPolicy.isWeakPassword(value)) {
            return fail(context, "Mật khẩu quá phổ biến, vui lòng chọn mật khẩu mạnh hơn");
        }
        return true;
    }

    private static boolean fail(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
        return false;
    }
}
