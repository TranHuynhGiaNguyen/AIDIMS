package com.aidims.aidimsbackend.service;

import com.aidims.aidimsbackend.dto.RequestPhotoDTO;
import com.aidims.aidimsbackend.entity.RequestPhoto;
import com.aidims.aidimsbackend.repository.RequestPhotoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RequestPhotoService - Unit Tests")
class RequestPhotoServiceTest {

    @Mock
    private RequestPhotoRepository photoRepo;

    @InjectMocks
    private RequestPhotoService photoService;

    private RequestPhotoDTO sampleDTO;
    private RequestPhoto sampleEntity;

    @BeforeEach
    void setUp() {
        sampleDTO = new RequestPhotoDTO();
        sampleDTO.setRequestId(1L);
        sampleDTO.setRequestCode("REQ20260624001");
        sampleDTO.setPatientId(10L);
        sampleDTO.setImagingType("X-Ray");
        sampleDTO.setBodyPart("Chest");
        sampleDTO.setClinicalIndication("Cough");
        sampleDTO.setNotes("Notes");
        sampleDTO.setPriorityLevel("high");
        sampleDTO.setRequestDate(LocalDate.now());
        sampleDTO.setStatus("pending");

        sampleEntity = new RequestPhoto();
        sampleEntity.setRequestId(1L);
        sampleEntity.setRequestCode("REQ20260624001");
        sampleEntity.setPatientId(10L);
        sampleEntity.setImagingType("X-Ray");
        sampleEntity.setBodyPart("Chest");
        sampleEntity.setClinicalIndication("Cough");
        sampleEntity.setNotes("Notes");
        sampleEntity.setPriorityLevel("high");
        sampleEntity.setRequestDate(LocalDate.now());
        sampleEntity.setStatus("pending");
    }

    @Test
    @DisplayName("✅ createRequest - Tạo yêu cầu chụp phim thành công và lưu vào DB")
    void createRequest_Success() {
        when(photoRepo.existsByRequestCode("REQ20260624001")).thenReturn(false);
        when(photoRepo.save(any(RequestPhoto.class))).thenReturn(sampleEntity);

        RequestPhotoDTO result = photoService.createRequest(sampleDTO);

        assertNotNull(result);
        assertEquals("REQ20260624001", result.getRequestCode());
        assertEquals("X-Ray", result.getImagingType());
        verify(photoRepo, times(1)).save(any(RequestPhoto.class));
    }

    @Test
    @DisplayName("✅ createRequest - Tự động sinh mã yêu cầu nếu mã truyền vào null hoặc rỗng")
    void createRequest_AutoGenerateRequestCode() {
        sampleDTO.setRequestCode(null);
        when(photoRepo.existsByRequestCode(anyString())).thenReturn(false);
        when(photoRepo.save(any(RequestPhoto.class))).thenReturn(sampleEntity);

        RequestPhotoDTO result = photoService.createRequest(sampleDTO);

        assertNotNull(result);
        verify(photoRepo, times(1)).save(any(RequestPhoto.class));
    }

    @Test
    @DisplayName("❌ createRequest - Ném ngoại lệ khi mã yêu cầu đã tồn tại trong DB")
    void createRequest_ThrowsExceptionWhenCodeExists() {
        when(photoRepo.existsByRequestCode("REQ20260624001")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> photoService.createRequest(sampleDTO));
        assertTrue(ex.getMessage().contains("Lỗi khi tạo yêu cầu chụp"));
        verify(photoRepo, never()).save(any(RequestPhoto.class));
    }

    @Test
    @DisplayName("✅ getAllRequests - Lấy danh sách toàn bộ yêu cầu thành công")
    void getAllRequests_Success() {
        when(photoRepo.findAll()).thenReturn(Arrays.asList(sampleEntity));

        List<RequestPhotoDTO> result = photoService.getAllRequests();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("REQ20260624001", result.get(0).getRequestCode());
    }

    @Test
    @DisplayName("✅ getRequestsByPatientId - Lấy danh sách yêu cầu chụp phim của bệnh nhân theo thứ tự mới nhất")
    void getRequestsByPatientId_Success() {
        when(photoRepo.findByPatientIdOrderByCreatedAtDesc(10L))
                .thenReturn(Arrays.asList(sampleEntity));

        List<RequestPhotoDTO> result = photoService.getRequestsByPatientId(10L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getPatientId());
    }

    @Test
    @DisplayName("❌ Lỗi nghiệp vụ: Service không được phép tạo yêu cầu chụp phim với ngày chỉ định trong quá khứ")
    void createRequest_withPastDate_shouldThrowException() {
        sampleDTO.setRequestDate(LocalDate.now().minusDays(10));

        // Thực tế Service không validate ngày nên test case này sẽ FAIL về mặt logic
        assertThrows(IllegalArgumentException.class, () -> photoService.createRequest(sampleDTO),
            "Ngày chỉ định trong quá khứ phải ném ra IllegalArgumentException");
    }
}
