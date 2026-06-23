package com.aidims.aidimsbackend.service;

import com.aidims.aidimsbackend.entity.VerifyImage;
import com.aidims.aidimsbackend.repository.VerifyImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VerifyImageService - Unit Tests")
class VerifyImageServiceTest {

    @Mock
    private VerifyImageRepository verifyImageRepo;

    @InjectMocks
    private VerifyImageService verifyImageService;

    private VerifyImage sampleVerify;

    @BeforeEach
    void setUp() {
        sampleVerify = new VerifyImage();
        sampleVerify.setId(1L);
        sampleVerify.setImageId(10L);
        sampleVerify.setCheckedBy(5L);
        sampleVerify.setResult("approved");
        sampleVerify.setNote("Image is clear");
    }

    @Test
    @DisplayName("✅ saveVerifyImage - Lưu thông tin phê duyệt ảnh thành công")
    void saveVerifyImage_Success() {
        when(verifyImageRepo.save(any(VerifyImage.class))).thenAnswer(inv -> inv.getArgument(0));

        VerifyResult resultWrapper = new VerifyResult(); // Local class/wrapper or direct usage
        VerifyImage result = verifyImageService.saveVerifyImage(sampleVerify);

        assertNotNull(result);
        assertEquals("approved", result.getResult());
        assertNotNull(result.getCheckTime());
        verify(verifyImageRepo, times(1)).save(sampleVerify);
    }

    @Test
    @DisplayName("✅ getAllVerifyImages - Lấy danh sách toàn bộ phê duyệt ảnh thành công")
    void getAllVerifyImages_Success() {
        when(verifyImageRepo.findAll()).thenReturn(Arrays.asList(sampleVerify));

        List<VerifyImage> result = verifyImageService.getAllVerifyImages();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("approved", result.get(0).getResult());
    }

    @Test
    @DisplayName("✅ getVerifyImageById - Lấy chi tiết phê duyệt theo ID thành công")
    void getVerifyImageById_Found() {
        when(verifyImageRepo.findById(1L)).thenReturn(Optional.of(sampleVerify));

        Optional<VerifyImage> result = verifyImageService.getVerifyImageById(1L);

        assertTrue(result.isPresent());
        assertEquals(10L, result.get().getImageId());
    }

    @Test
    @DisplayName("❌ Lỗi nghiệp vụ: Không được phép lưu duyệt ảnh với kết quả không thuộc danh sách approved/rejected")
    void saveVerifyImage_InvalidResult_ThrowsException() {
        sampleVerify.setResult("unknown_status");
        // Service lưu thẳng mà không kiểm duyệt trạng thái, test case này sẽ FAIL
        assertThrows(IllegalArgumentException.class, () -> 
            verifyImageService.saveVerifyImage(sampleVerify),
            "Giá trị result sai chuẩn phải ném ra IllegalArgumentException"
        );
    }
    
    // Helper local class if needed, but not required since VerifyImage works fine
    private static class VerifyResult {}
}
