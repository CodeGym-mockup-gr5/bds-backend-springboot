package com.codegym.bds_backend.auth.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.codegym.bds_backend.auth.dto.ChangePasswordRequest;
import com.codegym.bds_backend.auth.dto.RegisterRequest;
import com.codegym.bds_backend.auth.dto.SetPasswordRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ValidPasswordValidatorTest {
    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    private record PasswordHolder(
            @jakarta.validation.constraints.NotBlank(message = "Không được để trống")
            @ValidPassword
            String matKhau) {
    }

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Ab1!cdef",
            "RealiaTeam#94",
            "Dev@12345",
            "Aaaa@123",
            "Complex#Pass_2026!",
            "Ab1!Ab1!Ab1!Ab1!Ab1!Ab1!Ab1!Ab1!Ab1!Ab1!Ab1!Ab1!Ab1!Ab1!Ab1!Ab1!"
    })
    void shouldAcceptValidPasswords(String password) {
        Set<ConstraintViolation<PasswordHolder>> violations = validator.validate(new PasswordHolder(password));
        assertTrue(violations.isEmpty(), () -> "Kỳ vọng hợp lệ nhưng bị lỗi: " + violations);
    }

    @Test
    void shouldReportOnlyNotBlankWhenNullOrBlank() {
        Set<ConstraintViolation<PasswordHolder>> blankViolations = validator.validate(new PasswordHolder("   "));
        assertEquals(1, blankViolations.size());
        assertEquals("Không được để trống", blankViolations.iterator().next().getMessage());

        Set<ConstraintViolation<PasswordHolder>> nullViolations = validator.validate(new PasswordHolder(null));
        assertEquals(1, nullViolations.size());
        assertEquals("Không được để trống", nullViolations.iterator().next().getMessage());
    }

    @Test
    void shouldRejectLengthOutOfRange() {
        assertSingleMessage("Ab1!xyz", "Mật khẩu phải từ 8 đến 64 ký tự");
        assertSingleMessage("Ab1!Cd2@".repeat(8) + "x", "Mật khẩu phải từ 8 đến 64 ký tự");
    }

    @Test
    void shouldRejectWhitespaceInsidePassword() {
        assertSingleMessage("Mat Khau@1", "Mật khẩu không được chứa khoảng trắng");
        assertSingleMessage("Mat\tKhau@1", "Mật khẩu không được chứa khoảng trắng");
    }

    @Test
    void shouldRejectNonAsciiCharacters() {
        String asciiMessage = "Mật khẩu chỉ gồm chữ không dấu, số và ký tự đặc biệt cơ bản";
        assertSingleMessage("MậtKhẩu@1", asciiMessage);
        assertSingleMessage("PassWord@1😀", asciiMessage);
    }

    @Test
    void shouldReportMissingCharacterClasses() {
        assertSingleMessage("matkhau@1", "Mật khẩu cần có ít nhất: chữ in hoa");
        assertSingleMessage("MATKHAU@1", "Mật khẩu cần có ít nhất: chữ thường");
        assertSingleMessage("MatKhau@!", "Mật khẩu cần có ít nhất: chữ số");
        assertSingleMessage("MatKhau123", "Mật khẩu cần có ít nhất: ký tự đặc biệt");
        assertSingleMessage(
                "abcdefgh", "Mật khẩu cần có ít nhất: chữ in hoa, chữ số, ký tự đặc biệt");
    }

    @Test
    void shouldRejectFourConsecutiveIdenticalCharacters() {
        assertSingleMessage("Aaaaa@12", "Mật khẩu không được chứa 4 ký tự giống nhau liên tiếp");
        assertSingleMessage("Ab1111@c", "Mật khẩu không được chứa 4 ký tự giống nhau liên tiếp");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Admin@123",
            "aDmin@123",
            "Password@123",
            "P@ssw0rd1",
            "Qwerty@123",
            "MatKhau@123"
    })
    void shouldRejectWeakCommonPasswords(String weakPassword) {
        assertSingleMessage(weakPassword, "Mật khẩu quá phổ biến, vui lòng chọn mật khẩu mạnh hơn");
    }

    @Test
    void shouldApplyPolicyConsistentlyAcrossAllPasswordDtos() {
        RegisterRequest register = new RegisterRequest(
                "Nguyễn Văn An", "0912345678", "an@example.com", "012345678901", "khach01", "Ab1!cd", false);
        assertFalse(validator.validate(register).isEmpty());

        SetPasswordRequest setPassword = new SetPasswordRequest("token-123", "Admin@123");
        assertFalse(validator.validate(setPassword).isEmpty());

        ChangePasswordRequest changePassword = new ChangePasswordRequest("Old@1234", "Ab1!cd", "Ab1!cd");
        assertEquals(1, validator.validate(changePassword).size());
    }

    private static void assertSingleMessage(String password, String expectedMessage) {
        Set<ConstraintViolation<PasswordHolder>> violations = validator.validate(new PasswordHolder(password));
        assertEquals(1, violations.size());
        assertEquals(expectedMessage, violations.iterator().next().getMessage());
    }
}
