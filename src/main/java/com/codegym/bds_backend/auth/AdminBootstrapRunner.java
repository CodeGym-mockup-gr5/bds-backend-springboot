package com.codegym.bds_backend.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@RequiredArgsConstructor
public class AdminBootstrapRunner implements ApplicationRunner {
    private final AdminBootstrapService adminBootstrapService;

    @Override
    public void run(ApplicationArguments args) {
        adminBootstrapService.createAdminIfAbsent();
    }
}
