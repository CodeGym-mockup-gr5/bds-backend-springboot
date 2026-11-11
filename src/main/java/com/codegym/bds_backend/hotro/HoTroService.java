package com.codegym.bds_backend.hotro;

import com.codegym.bds_backend.config.AppProperties;
import com.codegym.bds_backend.hotro.dto.YeuCauHoTroRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HoTroService {
    private final JavaMailSender mailSender;
    private final AppProperties props;

    public void guiYeuCauHoTro(YeuCauHoTroRequest request) {
        log.info("Tiếp nhận yêu cầu hỗ trợ từ {} - SĐT: {} - Email: {}",
                request.hoTen(), request.soDienThoai(), request.email());
        sendEmailThongBaoAdmin(request);
    }

    @Async
    public void sendEmailThongBaoAdmin(YeuCauHoTroRequest request) {
        String adminEmail = (props.bootstrapAdmin() != null && props.bootstrapAdmin().email() != null)
                ? props.bootstrapAdmin().email()
                : props.mail().from();

        String noiDungMail = "Kính gửi Ban Quản Trị,\n\n"
                + "Hệ thống vừa nhận được một yêu cầu hỗ trợ mới từ khách hàng qua trang chủ:\n"
                + "- Họ tên: " + request.hoTen() + "\n"
                + "- Số điện thoại: " + request.soDienThoai() + "\n"
                + "- Email: " + request.email() + "\n"
                + "- Nội dung yêu cầu:\n" + request.noiDung() + "\n\n"
                + "Vui lòng kiểm tra và liên hệ hỗ trợ khách hàng sớm nhất.";

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(props.mail().from());
            message.setTo(adminEmail);
            message.setSubject("Yêu cầu hỗ trợ mới từ khách hàng: " + request.hoTen());
            message.setText(noiDungMail);
            mailSender.send(message);
            log.info("Đã gửi email thông báo yêu cầu hỗ trợ tới admin: {}", adminEmail);
        } catch (MailException e) {
            log.warn("Không thể gửi email thông báo yêu cầu hỗ trợ tới admin: {}", e.getMessage());
        }
    }
}
