package com.codegym.bds_backend.batdongsan;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.bat-dong-san")
public record BatDongSanProperties(
        @DefaultValue("10") int defaultPageSize,
        @DefaultValue("100") int pageMaxSize) {
}
