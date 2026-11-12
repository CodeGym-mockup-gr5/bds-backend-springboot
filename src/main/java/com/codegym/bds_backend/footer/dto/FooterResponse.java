package com.codegym.bds_backend.footer.dto;

import java.util.List;

public record FooterResponse(
        String tenThuongHieu,
        String slogan,
        String banQuyen,
        FooterContactResponse lienHe,
        List<FooterSectionResponse> danhSachChuyenMuc,
        List<FooterLinkResponse> mangXaHoi
) {
}
