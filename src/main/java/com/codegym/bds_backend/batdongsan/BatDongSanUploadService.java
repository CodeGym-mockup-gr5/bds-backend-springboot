package com.codegym.bds_backend.batdongsan;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.codegym.bds_backend.common.exception.BadRequestException;
import com.codegym.bds_backend.common.exception.BusinessException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class BatDongSanUploadService {

    private final Cloudinary cloudinary;

    public List<String> uploadHinhAnh(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new BadRequestException("Vui lòng chọn ít nhất 1 hình ảnh");
        }
        if (files.size() > 10) {
            throw new BadRequestException("Chỉ được tải lên tối đa 10 hình ảnh");
        }

        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            validateFile(file);
            try {
                Map<?, ?> result = cloudinary.uploader().upload(
                        file.getBytes(),
                        ObjectUtils.asMap("folder", "bat-dong-san"));
                String secureUrl = (String) result.get("secure_url");
                urls.add(secureUrl);
            } catch (IOException e) {
                throw new BusinessException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Tải ảnh lên hệ thống lưu trữ thất bại");
            }
        }
        return urls;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File ảnh không được để trống");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BadRequestException("Kích thước mỗi ảnh không được vượt quá 5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equalsIgnoreCase("image/jpeg")
                && !contentType.equalsIgnoreCase("image/png"))) {
            throw new BadRequestException("Chỉ chấp nhận định dạng ảnh JPG, PNG");
        }
    }
}