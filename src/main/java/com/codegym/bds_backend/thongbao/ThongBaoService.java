package com.codegym.bds_backend.thongbao;

import com.codegym.bds_backend.common.exception.NotFoundException;
import com.codegym.bds_backend.common.response.PageResponse;
import com.codegym.bds_backend.thongbao.dto.ThongBaoResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ThongBaoService {
    private final ThongBaoRepository thongBaoRepository;

    public PageResponse<ThongBaoResponse> findThongBao(String tuKhoa, Pageable pageable) {
        String normalized = (tuKhoa != null && !tuKhoa.isBlank()) ? tuKhoa.trim() : null;
        Page<ThongBao> page = thongBaoRepository.findThongBaoByTuKhoa(normalized, pageable);
        return PageResponse.of(page.map(ThongBaoResponse::fromEntity));
    }

    public List<ThongBaoResponse> findTopThongBaoMoiNhat(int limit) {
        int size = (limit <= 0 || limit > 20) ? 5 : limit;
        Pageable pageable = PageRequest.of(0, size);
        return thongBaoRepository.findTopMoiNhat(pageable).stream()
                .map(ThongBaoResponse::fromEntity)
                .toList();
    }

    public ThongBaoResponse findThongBaoById(Long id) {
        ThongBao thongBao = thongBaoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy thông báo với mã: " + id));
        return ThongBaoResponse.fromEntity(thongBao);
    }
}
