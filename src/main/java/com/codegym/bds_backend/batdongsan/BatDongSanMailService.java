package com.codegym.bds_backend.batdongsan;

import com.codegym.bds_backend.config.AppProperties;
import com.codegym.bds_backend.nhanvien.NhanVien;
import com.codegym.bds_backend.nhanvien.NhanVienRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatDongSanMailService {

    private final JavaMailSender mailSender;
    private final AppProperties props;
    private final NhanVienRepository nhanVienRepository;

    public void sendThongBaoTinMoiChoAdmin(String maBds, String diaChi, BigDecimal gia) {
        List<String> adminEmails = new ArrayList<>();
        List<NhanVien> admins = nhanVienRepository.findAllByLaAdminTrue();
        if (admins != null) {
            for (NhanVien nv : admins) {
                if (nv.getEmail() != null && !nv.getEmail().isBlank()) {
                    adminEmails.add(nv.getEmail());
                }
            }
        }

        if (adminEmails.isEmpty()) {
            String fallbackEmail = props.bootstrapAdmin() != null ? props.bootstrapAdmin().email() : null;
            if (fallbackEmail == null || fallbackEmail.isBlank()) {
                fallbackEmail = props.mail() != null ? props.mail().from() : null;
            }
            if (fallbackEmail != null && !fallbackEmail.isBlank()) {
                adminEmails.add(fallbackEmail);
            }
        }

        if (adminEmails.isEmpty()) {
            log.warn("Không tìm thấy email admin để gửi thông báo tin BĐS mới");
            return;
        }

        String subject = "Thông báo: Có tin đăng bất động sản mới [" + maBds + "]";
        String body = "Kính gửi Ban Quản lý,\n\n"
                + "Hệ thống vừa ghi nhận một tin đăng bất động sản mới:\n"
                + "- Mã BĐS: " + maBds + "\n"
                + "- Địa chỉ: " + diaChi + "\n"
                + "- Giá: " + gia + " VND\n\n"
                + "Trân trọng,\n"
                + "Hệ thống Quản lý Bất động sản";

        for (String email : adminEmails) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                if (props.mail() != null && props.mail().from() != null) {
                    message.setFrom(props.mail().from());
                }
                message.setTo(email);
                message.setSubject(subject);
                message.setText(body);
                mailSender.send(message);
            } catch (MailException e) {
                log.warn("Gửi email thông báo tin BĐS mới cho Admin {} thất bại: {}", email, e.getMessage());
            }
        }
    }
}
