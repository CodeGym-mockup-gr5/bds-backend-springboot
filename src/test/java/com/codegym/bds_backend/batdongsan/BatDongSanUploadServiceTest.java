package com.codegym.bds_backend.batdongsan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.codegym.bds_backend.common.exception.BadRequestException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class BatDongSanUploadServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @InjectMocks
    private BatDongSanUploadService uploadService;

    @Test
    void shouldUploadImagesSuccessfully() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "files",
                "test.jpg",
                "image/jpeg",
                "image content".getBytes());

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap()))
                .thenReturn(Map.of("secure_url", "https://res.cloudinary.com/test.jpg"));

        List<String> urls = uploadService.uploadHinhAnh(List.of(file));

        assertNotNull(urls);
        assertEquals(1, urls.size());
        assertEquals("https://res.cloudinary.com/test.jpg", urls.get(0));
    }

    @Test
    void shouldThrowBadRequestExceptionWhenFilesListIsEmpty() {
        assertThrows(BadRequestException.class, () -> uploadService.uploadHinhAnh(Collections.emptyList()));
    }

    @Test
    void shouldThrowBadRequestExceptionWhenMoreThanTenFiles() {
        MultipartFile dummy = new MockMultipartFile("f", "a.jpg", "image/jpeg", new byte[10]);
        List<MultipartFile> files = Collections.nCopies(11, dummy);
        assertThrows(BadRequestException.class, () -> uploadService.uploadHinhAnh(files));
    }

    @Test
    void shouldThrowBadRequestExceptionWhenInvalidContentType() {
        MockMultipartFile file = new MockMultipartFile("f", "a.pdf", "application/pdf", new byte[10]);
        assertThrows(BadRequestException.class, () -> uploadService.uploadHinhAnh(List.of(file)));
    }
}