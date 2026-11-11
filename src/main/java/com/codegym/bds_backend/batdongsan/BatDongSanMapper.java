package com.codegym.bds_backend.batdongsan;

import com.codegym.bds_backend.batdongsan.dto.BatDongSanListItemResponse;
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
}
