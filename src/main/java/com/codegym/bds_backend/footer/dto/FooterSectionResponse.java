package com.codegym.bds_backend.footer.dto;

import java.util.List;

public record FooterSectionResponse(
        String tieuDe,
        List<FooterLinkResponse> danhSachLienKet
) {
}
