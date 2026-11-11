package com.codegym.bds_backend.batdongsan;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class BatDongSanDangTinEventListener {

    private final BatDongSanMailService mailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBatDongSanDangTin(BatDongSanDangTinEvent event) {
        mailService.sendThongBaoTinMoiChoAdmin(event.maBds(), event.diaChi(), event.gia());
    }
}
