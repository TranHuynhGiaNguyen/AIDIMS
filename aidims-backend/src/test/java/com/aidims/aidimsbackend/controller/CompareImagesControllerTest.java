package com.aidims.aidimsbackend.controller;

import com.aidims.aidimsbackend.service.CompareImagesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompareImagesControllerTest - Mã hóa URL hình ảnh")
class CompareImagesControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CompareImagesService compareImagesService;

    @InjectMocks
    private CompareImagesController compareImagesController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(compareImagesController).build();
    }

    @Test
    @DisplayName("❌ Lỗi nghiệp vụ: Đường dẫn imageUrl chứa tên file có khoảng trắng phải được mã hóa URL thành %20")
    void searchPatientImages_shouldReturnUrlEncodedImageLinks() throws Exception {
        List<Map<String, Object>> mockImages = new ArrayList<>();
        Map<String, Object> img = new HashMap<>();
        img.put("id", 1L);
        img.put("fileName", "chest scan.dcm");
        // URL thô trả về từ Service (chưa được mã hóa dấu cách)
        img.put("imageUrl", "http://localhost:8080/api/dicom-viewer/image/chest scan.dcm");
        mockImages.add(img);

        Mockito.when(compareImagesService.searchByPatientCode(anyString())).thenReturn(mockImages);

        // Mong đợi đường dẫn trả về phải được mã hóa chuẩn thành "chest%20scan.dcm".
        // Test case sẽ FAIL vì API trả về dấu cách thô làm hỏng liên kết ảnh.
        mockMvc.perform(get("/api/compare-images/search")
                        .param("keyword", "BN001"))
                .andExpect(jsonPath("$[0].imageUrl", containsString("chest%20scan.dcm")));
    }
}
