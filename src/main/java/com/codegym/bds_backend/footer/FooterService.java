package com.codegym.bds_backend.footer;

import com.codegym.bds_backend.footer.dto.FooterContactResponse;
import com.codegym.bds_backend.footer.dto.FooterLinkResponse;
import com.codegym.bds_backend.footer.dto.FooterResponse;
import com.codegym.bds_backend.footer.dto.FooterSectionResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FooterService {
    private static final String TEN_THUONG_HIEU = "REALIA";
    private static final String SLOGAN = "Đồng hành cùng bạn trên hành trình an cư và đầu tư thịnh vượng";
    private static final String BAN_QUYEN =
            "© 2026 REALIA. Bản quyền thuộc về Trung tâm Môi giới Bất động sản REALIA.";

    private static final FooterContactResponse LIEN_HE = new FooterContactResponse(
            "Trung tâm Môi giới Bất động sản REALIA",
            "123 Đường Nguyễn Văn Linh, Quận Hải Châu, TP. Đà Nẵng",
            "1900 6868",
            "lienhe@realia.com.vn",
            "08:00 - 17:30 (Thứ 2 - Thứ 7)"
    );

    private static final List<FooterSectionResponse> DANH_SACH_CHUYEN_MUC = List.of(
            new FooterSectionResponse(
                    "Về REALIA",
                    List.of(
                            new FooterLinkResponse("Giới thiệu", "/gioi-thieu"),
                            new FooterLinkResponse("Bất động sản mua bán", "/bat-dong-san"),
                            new FooterLinkResponse("Dự án bất động sản", "/du-an")
                    )
            ),
            new FooterSectionResponse(
                    "Hỗ trợ khách hàng",
                    List.of(
                            new FooterLinkResponse("Hướng dẫn tài khoản", "/ho-tro/tai-khoan"),
                            new FooterLinkResponse("Hướng dẫn lập hợp đồng", "/ho-tro/lap-hop-dong"),
                            new FooterLinkResponse("Hướng dẫn đổi mật khẩu", "/ho-tro/doi-mat-khau"),
                            new FooterLinkResponse("Hướng dẫn giao dịch", "/ho-tro/giao-dich")
                    )
            ),
            new FooterSectionResponse(
                    "Chính sách & Quy định",
                    List.of(
                            new FooterLinkResponse("Quy chế hoạt động", "/chinh-sach/quy-che"),
                            new FooterLinkResponse("Điều khoản dịch vụ", "/chinh-sach/dieu-khoan"),
                            new FooterLinkResponse("Chính sách bảo mật", "/chinh-sach/bao-mat")
                    )
            )
    );

    private static final List<FooterLinkResponse> MANG_XA_HOI = List.of(
            new FooterLinkResponse("Facebook", "https://facebook.com/realia"),
            new FooterLinkResponse("YouTube", "https://youtube.com/realia"),
            new FooterLinkResponse("Zalo", "https://zalo.me/realia")
    );

    public FooterResponse findThongTinFooter() {
        return new FooterResponse(
                TEN_THUONG_HIEU,
                SLOGAN,
                BAN_QUYEN,
                LIEN_HE,
                DANH_SACH_CHUYEN_MUC,
                MANG_XA_HOI
        );
    }
}
