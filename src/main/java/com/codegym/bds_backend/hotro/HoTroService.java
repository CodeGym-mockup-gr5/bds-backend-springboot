package com.codegym.bds_backend.hotro;

import com.codegym.bds_backend.common.exception.NotFoundException;
import com.codegym.bds_backend.hotro.dto.HuongDanResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HoTroService {
    private static final Map<String, HuongDanResponse> DANH_SACH_HUONG_DAN = new LinkedHashMap<>();

    static {
        DANH_SACH_HUONG_DAN.put("tai-khoan", new HuongDanResponse(
                "tai-khoan",
                "Hướng dẫn sử dụng tài khoản",
                "Cho phép người dùng click vào để đọc hướng dẫn sử dụng tài khoản",
                "1. Đăng nhập bằng tên đăng nhập và mật khẩu được cấp.\n"
                        + "2. Với nhân viên mới: kiểm tra email lời mời kích hoạt và thiết lập mật khẩu lần đầu.\n"
                        + "3. Với khách hàng: có thể tự đăng ký tài khoản tại màn hình Đăng ký để quản lý nhu cầu "
                        + "mua và tin đăng.\n"
                        + "4. Sau khi đăng nhập thành công, người dùng có thể xem và chỉnh sửa thông tin cá nhân.",
                "/huong-dan/tai-khoan"
        ));

        DANH_SACH_HUONG_DAN.put("lap-hop-dong", new HuongDanResponse(
                "lap-hop-dong",
                "Hướng dẫn sử dụng lập hợp đồng",
                "Cho phép người dùng click vào để đọc tài liệu hướng dẫn lập hợp đồng",
                "1. Khách hàng và nhân viên liên hệ tư vấn, thống nhất các điều khoản mua bán hoặc đặt cọc.\n"
                        + "2. Nhân viên phụ trách hỗ trợ soạn hợp đồng theo biểu mẫu chuẩn của trung tâm.\n"
                        + "3. Đối soát kỹ giấy tờ pháp lý bất động sản, thông tin các bên và số tiền giao dịch.\n"
                        + "4. Tiến hành ký kết hợp đồng, trung tâm lưu trữ hồ sơ và hỗ trợ thủ tục công chứng.",
                "/huong-dan/lap-hop-dong"
        ));

        DANH_SACH_HUONG_DAN.put("doi-mat-khau", new HuongDanResponse(
                "doi-mat-khau",
                "Hướng dẫn đổi mật khẩu",
                "Cho phép người dùng click vào để đọc tài liệu hướng dẫn đổi mật khẩu",
                "1. Đổi mật khẩu chủ động: Đăng nhập vào hệ thống, vào mục 'Cá nhân' > chọn 'Đổi mật khẩu'.\n"
                        + "2. Quy định đổi mật khẩu: Mật khẩu mới từ 6 đến 32 ký tự, không được chứa khoảng trắng "
                        + "và không trùng mật khẩu cũ.\n"
                        + "3. Quên mật khẩu: Tại màn hình Đăng nhập, bấm 'Quên mật khẩu' và nhập tên đăng nhập.\n"
                        + "4. Hệ thống sẽ gửi email chứa liên kết đặt lại mật khẩu có hiệu lực trong 30 phút. "
                        + "Mật khẩu đặt lại yêu cầu độ dài từ 8 đến 64 ký tự.",
                "/huong-dan/doi-mat-khau"
        ));

        DANH_SACH_HUONG_DAN.put("giao-dich", new HuongDanResponse(
                "giao-dich",
                "Hướng dẫn giao dịch với khách hàng",
                "Cho phép người dùng click vào để đọc tài liệu hướng dẫn giao dịch với khách hàng",
                "1. Tiếp nhận và ghi nhận nhu cầu mua hoặc nhu cầu bán của khách hàng vào hệ thống.\n"
                        + "2. Sử dụng tính năng Gợi ý giao dịch để đối soát nhu cầu phù hợp với các bất động sản "
                        + "đang có sẵn.\n"
                        + "3. Tổ chức tư vấn, xem thực địa bất động sản và thương lượng giá cả giữa hai bên.\n"
                        + "4. Tiến hành tạo hồ sơ giao dịch, cập nhật tình trạng giao dịch sang THANH_CONG khi hoàn tất.",
                "/huong-dan/giao-dich"
        ));
    }

    public List<HuongDanResponse> findDanhSachHuongDan() {
        return List.copyOf(DANH_SACH_HUONG_DAN.values());
    }

    public HuongDanResponse findChiTietHuongDan(String maHuongDan) {
        if (maHuongDan == null || maHuongDan.isBlank()) {
            throw new NotFoundException("Không tìm thấy tài liệu hướng dẫn");
        }
        String key = maHuongDan.trim().toLowerCase(Locale.ROOT);
        HuongDanResponse huongDan = DANH_SACH_HUONG_DAN.get(key);
        if (huongDan == null) {
            throw new NotFoundException("Không tìm thấy tài liệu hướng dẫn với mã: " + maHuongDan);
        }
        return huongDan;
    }
}
