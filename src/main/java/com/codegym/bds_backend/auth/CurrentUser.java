package com.codegym.bds_backend.auth;

import com.codegym.bds_backend.common.enums.VaiTro;
import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Đọc vai trò của người đang gọi API từ SecurityContext (do JwtAuthenticationFilter đặt vào). */
@Component
public class CurrentUser {

    private static final Set<String> NOI_BO = Set.of(VaiTro.ADMIN.authority(), VaiTro.NHAN_VIEN.authority());

    /** Admin hoặc nhân viên. Khách chưa đăng nhập và khách hàng trả false. */
    public boolean isAdminOrNhanVien() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(NOI_BO::contains);
    }
}
