package com.aidims.aidimsbackend.controller;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.aidims.aidimsbackend.service.PatientService;
import com.aidims.aidimsbackend.service.SymptomRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("SymptomRecordControllerTest - Giới hạn thang điểm đau")
class SymptomRecordControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SymptomRecordService symptomRecordService;

    @Mock
    private PatientService patientService;

    @InjectMocks
    private SymptomRecordController symptomRecordController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(symptomRecordController).build();
    }

    @Test
    @DisplayName("❌ Lỗi nghiệp vụ: Thang điểm đau (Pain Scale) vượt quá phạm vi [0-10] phải bị từ chối")
    void createSymptomRecord_withInvalidPainScale_shouldReturnBadRequest() throws Exception {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("patient_id", "1");
        requestData.put("chief_complaint", "Đau đầu");
        requestData.put("severity_level", "Nặng");
        requestData.put("priority_level", "Khẩn cấp");
        requestData.put("pain_scale", 99);

        mockMvc.perform(post("/api/symptom-record/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestData)))
                .andExpect(status().isBadRequest());
    }
}
