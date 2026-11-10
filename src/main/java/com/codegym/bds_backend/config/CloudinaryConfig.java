package com.codegym.bds_backend.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class CloudinaryConfig {

    private final AppProperties props;

    @Bean
    public Cloudinary cloudinary() {
        AppProperties.Cloudinary c = props.cloudinary();
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", c.cloudName(),
                "api_key", c.apiKey(),
                "api_secret", c.apiSecret(),
                "secure", true));
    }
}
