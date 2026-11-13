package com.codegym.bds_backend.batdongsan;

import com.codegym.bds_backend.batdongsan.dto.BatDongSanDetailResponse;
import com.codegym.bds_backend.batdongsan.dto.BatDongSanListItemResponse;
import com.codegym.bds_backend.batdongsan.dto.ChuSoHuuResponse;
import com.codegym.bds_backend.batdongsan.dto.HinhAnhResponse;
import com.codegym.bds_backend.khachhang.KhachHang;
import org.springframework.stereotype.Component;

@Component
public class BatDongSanMapper {

    public BatDongSanListItemResponse toListItemResponse(BatDongSan bds, String anhDaiDien, boolean hienChuSoHuu) {
        KhachHang chuSoHuu = hienChuSoHuu ? bds.getKhachHang() : null;
        return new BatDongSanListItemResponse(
                bds.getId(),
                bds.getMaBds(),
                chuSoHuu == null ? null : chuSoHuu.getId(),
                chuSoHuu == null ? null : chuSoHuu.getMaKh(),
                chuSoHuu == null ? null : chuSoHuu.getHoTen(),
                bds.getLoaiBds(),
                bds.getDiaChi(),
                bds.getViTri(),
                bds.getHuong(),
                bds.getDienTich(),
                bds.getGia(),
                bds.getGiaM2(),
                bds.getTinhTrang(),
                bds.getGhiChu(),
                bds.getNgayDang(),
                anhDaiDien);
    }

    public BatDongSanDetailResponse toDetailResponse(BatDongSan bds, boolean hienChuSoHuu, boolean hienSoDienThoai) {
        return new BatDongSanDetailResponse(
                bds.getId(),
                bds.getMaBds(),
                bds.getLoaiBds(),
                bds.getDiaChi(),
                bds.getViTri(),
                bds.getHuong(),
                bds.getDienTich(),
                bds.getGia(),
                bds.getGiaM2(),
                bds.getPhapLy(),
                bds.getTinhTrang(),
                bds.getGhiChu(),
                bds.getNgayDang(),
                bds.getHinhAnh().stream().map(this::toHinhAnhResponse).toList(),
                hienChuSoHuu ? toChuSoHuuResponse(bds.getKhachHang(), hienSoDienThoai) : null);
    }

    private HinhAnhResponse toHinhAnhResponse(HinhAnhBds hinhAnh) {
        return new HinhAnhResponse(hinhAnh.getId(), hinhAnh.getDuongDan(), hinhAnh.getThuTu());
    }

    private ChuSoHuuResponse toChuSoHuuResponse(KhachHang chuSoHuu, boolean hienSoDienThoai) {
        if (chuSoHuu == null) {
            return null;
        }
        return new ChuSoHuuResponse(
                chuSoHuu.getId(),
                chuSoHuu.getMaKh(),
                chuSoHuu.getHoTen(),
                hienSoDienThoai ? chuSoHuu.getSoDienThoai() : null);
    }
}
