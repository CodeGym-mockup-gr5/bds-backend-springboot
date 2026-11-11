package com.codegym.bds_backend.auth;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Order(2)
@ConditionalOnProperty("app.dev.default-password")
public class DevPasswordInitializer implements ApplicationRunner {
    private final TaiKhoanRepository taiKhoanRepository;
    private final PasswordEncoder passwordEncoder;
    private final String defaultPassword;

    public DevPasswordInitializer(
            TaiKhoanRepository taiKhoanRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.dev.default-password}") String defaultPassword) {
        this.taiKhoanRepository = taiKhoanRepository;
        this.passwordEncoder = passwordEncoder;
        this.defaultPassword = defaultPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<TaiKhoan> chuaCoMatKhau = taiKhoanRepository.findAllByMatKhauHashIsNull();
        String hash = passwordEncoder.encode(defaultPassword);
        chuaCoMatKhau.forEach(taiKhoan -> taiKhoan.setMatKhauHash(hash));
        log.info("Đã đặt mật khẩu mặc định cho {} tài khoản (môi trường phát triển)", chuaCoMatKhau.size());
    }
}
