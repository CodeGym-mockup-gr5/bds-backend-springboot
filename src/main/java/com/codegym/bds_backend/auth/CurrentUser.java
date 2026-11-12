package com.codegym.bds_backend.auth;

import com.codegym.bds_backend.common.enums.VaiTro;
import com.codegym.bds_backend.khachhang.KhachHang;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUser {

    private static final Set<String> NOI_BO = Set.of(VaiTro.ADMIN.authority(), VaiTro.NHAN_VIEN.authority());

    private final TaiKhoanRepository taiKhoanRepository;

    public boolean isAdminOrNhanVien() {
        return findAuthentication()
                .map(authentication -> authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(NOI_BO::contains))
                .orElse(false);
    }

    public Optional<Long> findKhachHangId() {
        if (!hasAuthority(VaiTro.KHACH_HANG.authority())) {
            return Optional.empty();
        }
        return findAuthentication()
                .map(Authentication::getPrincipal)
                .filter(JwtPrincipal.class::isInstance)
                .map(JwtPrincipal.class::cast)
                .map(JwtPrincipal::userId)
                .flatMap(taiKhoanRepository::findWithChuSoHuuById)
                .map(TaiKhoan::getKhachHang)
                .map(KhachHang::getId);
    }

    private boolean hasAuthority(String authority) {
        return findAuthentication()
                .map(authentication -> authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(authority::equals))
                .orElse(false);
    }

    private Optional<Authentication> findAuthentication() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated);
    }
}
