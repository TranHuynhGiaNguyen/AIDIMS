package com.aidims.aidimsbackend.controller;

import com.aidims.aidimsbackend.entity.Patient;
import com.aidims.aidimsbackend.repository.PatientRepository;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("PatientControllerTest - Kiểm duyệt sinh hiệu")
class PatientControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientController patientController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(patientController).build();
    }

    @Test
    @DisplayName("❌ Lỗi nghiệp vụ: SpO2 vượt quá giới hạn 100% vẫn được chấp nhận lưu trữ")
    void testGetPatientById_withInvalidOxygenSaturation_shouldFailValidation() {
        Patient invalidPatient = new Patient();
        invalidPatient.setPatient_id(1L);
        invalidPatient.setFull_name("Bệnh nhân A");
        invalidPatient.setOxygen_saturation(150);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(invalidPatient));

        Patient result = patientController.getPatientById(1L);

        assertTrue(result.getOxygen_saturation() <= 100,
                "Chỉ số SpO2 của bệnh nhân không thể lớn hơn 100%!");
    }
}
