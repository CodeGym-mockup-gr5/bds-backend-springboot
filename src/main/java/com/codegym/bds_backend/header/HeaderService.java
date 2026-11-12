package com.codegym.bds_backend.header;

import com.codegym.bds_backend.auth.TaiKhoan;
import com.codegym.bds_backend.auth.TaiKhoanRepository;
import com.codegym.bds_backend.common.enums.VaiTro;
import com.codegym.bds_backend.header.dto.ActionButtonResponse;
import com.codegym.bds_backend.header.dto.HeaderResponse;
import com.codegym.bds_backend.header.dto.HeaderUserResponse;
import com.codegym.bds_backend.header.dto.MenuItemResponse;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeaderService {
    private static final String TEN_THUONG_HIEU = "REALIA";
    private static final String LOGO_URL = "/assets/images/logo.png";

    private static final List<MenuItemResponse> MENU_MAC_DINH = List.of(
            new MenuItemResponse("Mua bán", "/bat-dong-san"),
            new MenuItemResponse("Dự án", "/du-an"),
            new MenuItemResponse("Hỗ trợ", "/ho-tro")
    );

    private final TaiKhoanRepository taiKhoanRepository;

    public HeaderResponse findThongTinHeader(Long userId) {
        if (userId == null) {
            return taoHeaderChuaDangNhap();
        }

        return taiKhoanRepository.findWithChuSoHuuById(userId)
                .map(this::taoHeaderDaDangNhap)
                .orElseGet(this::taoHeaderChuaDangNhap);
    }

    private HeaderResponse taoHeaderChuaDangNhap() {
        List<ActionButtonResponse> hanhDong = List.of(
                new ActionButtonResponse("DANG_KY", "Đăng ký", "BUTTON", "/dang-ky"),
                new ActionButtonResponse("DANG_NHAP", "Đăng nhập", "BUTTON", "/dang-nhap")
        );

        return new HeaderResponse(
                LOGO_URL,
                TEN_THUONG_HIEU,
                false,
                false,
                false,
                MENU_MAC_DINH,
                hanhDong,
                null
        );
    }

    private HeaderResponse taoHeaderDaDangNhap(TaiKhoan taiKhoan) {
        String hoTen = taiKhoan.hoTenChuSoHuu() != null
                ? taiKhoan.hoTenChuSoHuu()
                : taiKhoan.getTenDangNhap();
        String kyTuDaiDien = taoKyTuDaiDien(hoTen);
        List<String> vaiTro = layDanhSachVaiTro(taiKhoan);

        HeaderUserResponse userResponse = new HeaderUserResponse(
                taiKhoan.getId(),
                taiKhoan.getTenDangNhap(),
                hoTen,
                kyTuDaiDien,
                vaiTro
        );

        List<ActionButtonResponse> hanhDong = List.of(
                new ActionButtonResponse("THONG_BAO", "Thông báo", "ICON", "/thong-bao"),
                new ActionButtonResponse("DANG_TIN", "+ Đăng tin", "BUTTON", "/dang-tin")
        );

        return new HeaderResponse(
                LOGO_URL,
                TEN_THUONG_HIEU,
                true,
                true,
                true,
                MENU_MAC_DINH,
                hanhDong,
                userResponse
        );
    }

    private List<String> layDanhSachVaiTro(TaiKhoan taiKhoan) {
        List<String> vaiTro = new ArrayList<>();
        if (taiKhoan.getNhanVien() != null) {
            vaiTro.add(VaiTro.NHAN_VIEN.authority());
            if (taiKhoan.getNhanVien().isLaAdmin()) {
                vaiTro.add(VaiTro.ADMIN.authority());
            }
        }
        if (taiKhoan.getKhachHang() != null) {
            vaiTro.add(VaiTro.KHACH_HANG.authority());
        }
        return List.copyOf(vaiTro);
    }

    private String taoKyTuDaiDien(String hoTen) {
        if (hoTen == null || hoTen.isBlank()) {
            return "U";
        }
        String[] tu = hoTen.trim().split("\\s+");
        if (tu.length == 1) {
            return tu[0].substring(0, Math.min(2, tu[0].length())).toUpperCase();
        }
        String dau = tu[0].substring(0, 1);
        String cuoi = tu[tu.length - 1].substring(0, 1);
        return (dau + cuoi).toUpperCase();
    }
}
