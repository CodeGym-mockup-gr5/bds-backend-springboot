package com.codegym.bds_backend.config;

import com.codegym.bds_backend.auth.JwtAuthenticationFilter;
import com.codegym.bds_backend.auth.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/** Bảo mật dùng JWT, không giữ session. Phân quyền chi tiết bằng @PreAuthorize ở từng endpoint. */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AppProperties props;
    private final JwtService jwtService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Tạm thời chưa có người dùng nào. Khi làm module auth sẽ thay bằng bản đọc từ bảng tai_khoan.
     * Khai báo ở đây để Spring Boot không tự tạo tài khoản "user" kèm mật khẩu ngẫu nhiên.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("Chưa cấu hình nguồn tài khoản");
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Công khai: đăng nhập, đăng ký, quên mật khẩu, Swagger
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        // Tin mới đăng chỉ dành cho Admin (đặt trước quy tắc GET công khai bên dưới)
                        .requestMatchers("/api/v1/bat-dong-san/moi-dang").hasRole("ADMIN")
                        // Xem danh sách và chi tiết BĐS công khai theo quy ước C.8; chỉnh lại nếu SRS khác
                        .requestMatchers(HttpMethod.GET, "/api/v1/bat-dong-san/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) ->
                                writeError(res, HttpServletResponse.SC_UNAUTHORIZED, "Vui lòng đăng nhập"))
                        .accessDeniedHandler((req, res, e) -> writeError(
                                res, HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền thực hiện chức năng này")))
                // Tạo bằng new, không đánh dấu @Component, để Spring Boot không đăng ký filter hai lần
                .addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(props.cors().allowedOrigins());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /** Ghi JSON cùng khuôn ApiResponse. Thông báo là chuỗi cố định, không chứa ký tự cần escape. */
    private static void writeError(HttpServletResponse res, int status, String message) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json");
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        res.getWriter().write("{\"success\":false,\"data\":null,\"message\":\"" + message + "\",\"errors\":null}");
    }
}
