package com.codegym.bds_backend.auth;

import com.codegym.bds_backend.config.AppProperties;
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
public class MailService {
    private final JavaMailSender mailSender;
    private final AppProperties props;

    @Async
    public void sendLoiMoi(String to, String hoTen, String token, int soGioHieuLuc) {
        String body = "Xin chào " + hoTen + ",\n\n"
                + "Trung tâm môi giới bất động sản đã tạo tài khoản cho bạn. "
                + "Vui lòng đặt mật khẩu tại liên kết sau (có hiệu lực " + soGioHieuLuc + " giờ):\n"
                + buildLink(token) + "\n\n"
                + "Nếu bạn không yêu cầu, hãy bỏ qua email này.";
        send(to, "Lời mời kích hoạt tài khoản", body);
    }

    @Async
    public void sendDatLaiMatKhau(String to, String hoTen, String token, int soPhutHieuLuc) {
        String body = "Xin chào " + hoTen + ",\n\n"
                + "Bạn vừa yêu cầu đặt lại mật khẩu. Vui lòng truy cập liên kết sau "
                + "(có hiệu lực " + soPhutHieuLuc + " phút):\n"
                + buildLink(token) + "\n\n"
                + "Nếu bạn không yêu cầu, hãy bỏ qua email này.";
        send(to, "Đặt lại mật khẩu", body);
    }

    private String buildLink(String token) {
        return props.mail().frontendUrl() + "/dat-mat-khau?token=" + token;
    }

    private void send(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(props.mail().from());
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (MailException e) {
            log.error("Gửi email '{}' thất bại: {}", subject, e.getMessage());
        }
    }
}
