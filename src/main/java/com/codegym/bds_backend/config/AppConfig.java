package com.codegym.bds_backend.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/** Bật đọc cấu hình app.* và xử lý bất đồng bộ (gửi email bằng @Async). */
@Configuration
@EnableAsync
@EnableConfigurationProperties(AppProperties.class)
public class AppConfig {
}
