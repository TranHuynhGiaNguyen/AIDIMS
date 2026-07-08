package com.aidims.aidimsbackend.service;

import com.aidims.aidimsbackend.entity.VerifyImage;
import com.aidims.aidimsbackend.repository.VerifyImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

    // =============================================================
    // NHOM 1 - SAVE VERIFY IMAGE - HOP LE (TC1-TC4)
    // =============================================================

    @Nested
    @DisplayName("Nhom 1 - saveVerifyImage - Hop le")
    class SaveSuccessTests {

        @Test
        @DisplayName("TC1 - Luu voi result='approved' thanh cong")
        void tc1_Save_Approved() {
            sampleVerify.setResult("approved");
            when(verifyImageRepo.save(any(VerifyImage.class))).thenAnswer(inv -> inv.getArgument(0));

            VerifyImage result = verifyImageService.saveVerifyImage(sampleVerify);

            assertNotNull(result);
            assertEquals("approved", result.getResult());
            assertNotNull(result.getCheckTime());
            verify(verifyImageRepo, times(1)).save(any(VerifyImage.class));
        }

        @Test
        @DisplayName("TC2 - Luu voi result='rejected' thanh cong")
        void tc2_Save_Rejected() {
            sampleVerify.setResult("rejected");
            when(verifyImageRepo.save(any(VerifyImage.class))).thenAnswer(inv -> inv.getArgument(0));

            VerifyImage result = verifyImageService.saveVerifyImage(sampleVerify);

            assertNotNull(result);
            assertEquals("rejected", result.getResult());
            assertNotNull(result.getCheckTime());
            verify(verifyImageRepo, times(1)).save(any(VerifyImage.class));
        }

        @Test
        @DisplayName("TC3 - Luu voi result='APPROVED' (chu hoa) thanh cong - case-insensitive")
        void tc3_Save_Approved_UpperCase() {
            sampleVerify.setResult("APPROVED");
            when(verifyImageRepo.save(any(VerifyImage.class))).thenAnswer(inv -> inv.getArgument(0));

            VerifyImage result = verifyImageService.saveVerifyImage(sampleVerify);

            assertNotNull(result);
            assertEquals("APPROVED", result.getResult());
            assertNotNull(result.getCheckTime());
            verify(verifyImageRepo, times(1)).save(any(VerifyImage.class));
        }

        @Test
        @DisplayName("TC4 - Luu voi result='REJECTED' (chu hoa) thanh cong - case-insensitive")
        void tc4_Save_Rejected_UpperCase() {
            sampleVerify.setResult("REJECTED");
            when(verifyImageRepo.save(any(VerifyImage.class))).thenAnswer(inv -> inv.getArgument(0));

            VerifyImage result = verifyImageService.saveVerifyImage(sampleVerify);

            assertNotNull(result);
            assertEquals("REJECTED", result.getResult());
            assertNotNull(result.getCheckTime());
            verify(verifyImageRepo, times(1)).save(any(VerifyImage.class));
        }
    }

    // =============================================================
    // NHOM 2 - SAVE VERIFY IMAGE - KHONG HOP LE (TC5-TC6)
    // =============================================================

    @Nested
    @DisplayName("Nhom 2 - saveVerifyImage - Khong hop le")
    class SaveInvalidTests {

        @Test
        @DisplayName("TC5 - result=null -> IllegalArgumentException")
        void tc5_Save_Result_Null() {
            sampleVerify.setResult(null);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> verifyImageService.saveVerifyImage(sampleVerify));

            assertEquals("Kết quả duyệt ảnh không hợp lệ!", ex.getMessage());
            verify(verifyImageRepo, never()).save(any(VerifyImage.class));
        }

        @Test
        @DisplayName("TC6 - result='unknown' -> IllegalArgumentException")
        void tc6_Save_Result_Unknown() {
            sampleVerify.setResult("unknown");

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> verifyImageService.saveVerifyImage(sampleVerify));

            assertEquals("Kết quả duyệt ảnh không hợp lệ!", ex.getMessage());
            verify(verifyImageRepo, never()).save(any(VerifyImage.class));
        }
    }

    // =============================================================
    // NHOM 3 - GET ALL VERIFY IMAGES (TC7)
    // =============================================================

    @Nested
    @DisplayName("Nhom 3 - getAllVerifyImages")
    class GetAllTests {

        @Test
        @DisplayName("TC7 - Lay danh sach toan bo phe duyet anh thanh cong")
        void tc7_GetAll_Success() {
            when(verifyImageRepo.findAll()).thenReturn(Arrays.asList(sampleVerify));

            List<VerifyImage> result = verifyImageService.getAllVerifyImages();

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("approved", result.get(0).getResult());
            verify(verifyImageRepo, times(1)).findAll();
        }
    }

    // =============================================================
    // NHOM 4 - GET VERIFY IMAGE BY ID (TC8)
    // =============================================================

    @Nested
    @DisplayName("Nhom 4 - getVerifyImageById")
    class GetByIdTests {

        @Test
        @DisplayName("TC8 - Lay chi tiet phe duyet theo ID thanh cong")
        void tc8_GetById_Found() {
            when(verifyImageRepo.findById(1L)).thenReturn(Optional.of(sampleVerify));

            Optional<VerifyImage> result = verifyImageService.getVerifyImageById(1L);

            assertTrue(result.isPresent());
            assertEquals(10L, result.get().getImageId());
            verify(verifyImageRepo, times(1)).findById(1L);
        }
    }
}
