package com.codegym.bds_backend.hotro;

import com.codegym.bds_backend.common.exception.NotFoundException;
import com.codegym.bds_backend.hotro.dto.HuongDanResponse;
import java.util.LinkedHashMap;
import java.util.List;
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
                        + "3. Với khách hàng: có thể tự đăng ký tài khoản tại màn hình Đăng ký để quản lý nhu cầu mua "
                        + "và tin đăng.\n"
                        + "4. Sau khi đăng nhập thành công, người dùng có thể xem và chỉnh sửa thông tin cá nhân.",
                "/huong-dan/tai-khoan"
        ));

        DANH_SACH_HUONG_DAN.put("lap-hop-dong", new HuongDanResponse(
                "lap-hop-dong",
                "Hướng dẫn sử dụng lập hợp đồng",
                "Cho phép người dùng click vào để đọc tài liệu hướng dẫn lập hợp đồng",
                "1. Truy cập vào mục Quản lý biểu mẫu hoặc Quản lý giao dịch.\n"
                        + "2. Tải về mẫu Hợp đồng đặt cọc hoặc Hợp đồng ủy quyền môi giới chuẩn của trung tâm.\n"
                        + "3. Điền đầy đủ thông tin bên mua, bên bán, thông tin pháp lý bất động sản và số tiền giao dịch.\n"
                        + "4. Ghi nhận giao dịch trên hệ thống để theo dõi tiến độ và tỷ lệ hoa hồng.",
                "/huong-dan/lap-hop-dong"
        ));

        DANH_SACH_HUONG_DAN.put("doi-mat-khau", new HuongDanResponse(
                "doi-mat-khau",
                "Hướng dẫn đổi mật khẩu",
                "Cho phép người dùng click vào để đọc tài liệu hướng dẫn đổi mật khẩu",
                "1. Đổi mật khẩu chủ động: Đăng nhập vào hệ thống, vào mục 'Thông tin cá nhân' > chọn 'Đổi mật khẩu'.\n"
                        + "2. Nhập mật khẩu hiện tại và mật khẩu mới (tối thiểu 6 ký tự, gồm cả chữ và số).\n"
                        + "3. Quên mật khẩu: Tại màn hình Đăng nhập, bấm 'Quên mật khẩu' và nhập tên đăng nhập.\n"
                        + "4. Hệ thống sẽ gửi email chứa liên kết đặt lại mật khẩu có hiệu lực trong 30 phút.",
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
        if (maHuongDan == null || !DANH_SACH_HUONG_DAN.containsKey(maHuongDan.trim().toLowerCase())) {
            throw new NotFoundException("Không tìm thấy tài liệu hướng dẫn với mã: " + maHuongDan);
        }
        return DANH_SACH_HUONG_DAN.get(maHuongDan.trim().toLowerCase());
    }
}
