package com.aidims.aidimsbackend.controller;

import com.aidims.aidimsbackend.entity.VerifyImage;
import com.aidims.aidimsbackend.repository.DicomImportRepository;
import com.aidims.aidimsbackend.service.VerifyImageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("VerifyImageController - Unit Tests")
class VerifyImageControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private VerifyImageService verifyImageService;

    @Mock
    private DicomImportRepository dicomImportRepository;

    @InjectMocks
    private VerifyImageController verifyImageController;

    private VerifyImage sampleVerify;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(verifyImageController).build();

        sampleVerify = new VerifyImage();
        sampleVerify.setId(1L);
        sampleVerify.setImageId(100L);
        sampleVerify.setCheckedBy(5L);
        sampleVerify.setResult("approved");
        sampleVerify.setNote("Clear image");
    }

    @Test
    @DisplayName("✅ saveVerifyImage - Thành công khi đầu vào hợp lệ")
    void saveVerifyImage_Success() throws Exception {
        when(dicomImportRepository.existsById(100L)).thenReturn(true);
        when(verifyImageService.saveVerifyImage(any(VerifyImage.class))).thenReturn(sampleVerify);

        mockMvc.perform(post("/api/verify-image/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleVerify)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.result").value("approved"));
    }

    @Test
    @DisplayName("❌ saveVerifyImage - Lỗi 400 khi thiếu trường bắt buộc")
    void saveVerifyImage_MissingFields() throws Exception {
        VerifyImage invalid = new VerifyImage(); // Thiếu imageId, checkedBy, result

        mockMvc.perform(post("/api/verify-image/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("❌ saveVerifyImage - Lỗi 400 khi không tìm thấy ảnh")
    void saveVerifyImage_ImageNotFound() throws Exception {
        when(dicomImportRepository.existsById(100L)).thenReturn(false);

        mockMvc.perform(post("/api/verify-image/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleVerify)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("✅ getAllVerifyImages - Lấy danh sách phê duyệt thành công")
    void getAllVerifyImages_Success() throws Exception {
        when(verifyImageService.getAllVerifyImages()).thenReturn(Arrays.asList(sampleVerify));

        mockMvc.perform(get("/api/verify-image/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("✅ getVerifyImageById - Lấy chi tiết phê duyệt thành công")
    void getVerifyImageById_Found() throws Exception {
        when(verifyImageService.getVerifyImageById(1L)).thenReturn(Optional.of(sampleVerify));

        mockMvc.perform(get("/api/verify-image/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageId").value(100L));
    }

    @Test
    @DisplayName("❌ getVerifyImageById - Trả về 404 khi không tìm thấy")
    void getVerifyImageById_NotFound() throws Exception {
        when(verifyImageService.getVerifyImageById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/verify-image/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("✅ updateImageStatus - Cập nhật trạng thái duyệt ảnh thành công")
    void updateImageStatus_Success() throws Exception {
        when(verifyImageService.getVerifyImageById(1L)).thenReturn(Optional.of(sampleVerify));
        when(verifyImageService.saveVerifyImage(any(VerifyImage.class))).thenReturn(sampleVerify);

        Map<String, String> payload = Map.of("status", "rejected");

        mockMvc.perform(patch("/api/verify-image/{id}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("rejected"));
    }

    @Test
    @DisplayName("✅ getVerifyStatusSummary - Thống kê số lượng duyệt/chưa duyệt")
    void getVerifyStatusSummary_Success() throws Exception {
        when(verifyImageService.getAllVerifyImages()).thenReturn(Arrays.asList(sampleVerify));

        mockMvc.perform(get("/api/verify-image/status-summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.daDuyet").value(1))
                .andExpect(jsonPath("$.chuaDuyet").value(0))
                .andExpect(jsonPath("$.tong").value(1));
    }

    @Test
    @DisplayName("❌ Lỗi nghiệp vụ: Cập nhật trạng thái duyệt của phim thành rỗng hoặc sai định dạng phải trả về thông báo lỗi cụ thể")
    void updateImageStatus_InvalidPayload_ReturnsErrorMessage() throws Exception {
        Map<String, String> payload = Map.of("status", "   "); // rỗng

        when(verifyImageService.getVerifyImageById(1L)).thenReturn(Optional.of(sampleVerify));

        // Controller thực tế chấp nhận mọi chuỗi và trả về 200 OK cùng success.
        // Test mong đợi trả về json có kết quả lỗi logic nhưng thực tế thành công nên sẽ FAIL.
        mockMvc.perform(patch("/api/verify-image/{id}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(jsonPath("$.status").value("error"));
    }
}
