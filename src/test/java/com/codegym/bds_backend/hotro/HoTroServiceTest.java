package com.codegym.bds_backend.hotro;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codegym.bds_backend.config.AppProperties;
import com.codegym.bds_backend.hotro.dto.YeuCauHoTroRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class HoTroServiceTest {
    @Mock
    private JavaMailSender mailSender;

    @Mock
    private AppProperties props;

    @InjectMocks
    private HoTroService hoTroService;

    private YeuCauHoTroRequest request;

    @BeforeEach
    void setUp() {
        request = new YeuCauHoTroRequest(
                "Trần Văn Hùng",
                "0912345678",
                "hung.tv@example.com",
                "Tôi cần tư vấn mua đất nền khu vực quận Cẩm Lệ"
        );
    }

    @Test
    void guiYeuCauHoTro_thanhCong_guiMailAdmin() {
        AppProperties.BootstrapAdmin admin = new AppProperties.BootstrapAdmin(
                "admin", "Quản trị viên", "admin@realia.local", "0900000000");
        AppProperties.Mail mail = new AppProperties.Mail(
                "no-reply@realia.local", "http://localhost:5173");

        when(props.bootstrapAdmin()).thenReturn(admin);
        when(props.mail()).thenReturn(mail);

        assertDoesNotThrow(() -> hoTroService.guiYeuCauHoTro(request));
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void guiYeuCauHoTro_loiGuiMail_vanKhongNemException() {
        AppProperties.BootstrapAdmin admin = new AppProperties.BootstrapAdmin(
                "admin", "Quản trị viên", "admin@realia.local", "0900000000");
        AppProperties.Mail mail = new AppProperties.Mail(
                "no-reply@realia.local", "http://localhost:5173");

        when(props.bootstrapAdmin()).thenReturn(admin);
        when(props.mail()).thenReturn(mail);
        doThrow(new MailSendException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() -> hoTroService.guiYeuCauHoTro(request));
        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}
