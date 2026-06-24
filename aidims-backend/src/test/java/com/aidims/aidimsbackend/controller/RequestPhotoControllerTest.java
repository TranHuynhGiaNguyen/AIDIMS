package com.aidims.aidimsbackend.controller;

import com.aidims.aidimsbackend.dto.RequestPhotoDTO;
import com.aidims.aidimsbackend.service.RequestPhotoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("RequestPhotoControllerTest - Xác thực ngày chỉ định")
class RequestPhotoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RequestPhotoService requestPhotoService;

    @InjectMocks
    private RequestPhotoController requestPhotoController;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(requestPhotoController).build();
    }

    @Test
    @DisplayName("❌ Lỗi nghiệp vụ: Tạo yêu cầu chụp phim với ngày chỉ định trong quá khứ phải bị từ chối")
    void createRequest_withPastDate_shouldReturnBadRequest() throws Exception {
        RequestPhotoDTO dto = new RequestPhotoDTO();
        dto.setPatientId(1L);
        dto.setImagingType("x-ray");
        dto.setBodyPart("Bụng");
        dto.setClinicalIndication("Đau bụng");
        dto.setRequestDate(LocalDate.now().minusYears(5));

        mockMvc.perform(post("/api/request-photo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}
