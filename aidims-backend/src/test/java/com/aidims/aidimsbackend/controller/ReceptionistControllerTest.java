package com.aidims.aidimsbackend.controller;

import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;

import static org.hamcrest.Matchers.containsString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.aidims.aidimsbackend.entity.Doctor;
import com.aidims.aidimsbackend.entity.Patient;
import com.aidims.aidimsbackend.service.ReceptionistService;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReceptionistController - Unit Tests")
class ReceptionistControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReceptionistService receptionistService;

    @InjectMocks
    private ReceptionistController receptionistController;

    @BeforeEach
    void setUp() {
        // Đã thêm cấu hình UTF-8 mặc định để sửa lỗi dấu tiếng Việt
        mockMvc = MockMvcBuilders.standaloneSetup(receptionistController)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .build();
    }

    // =============================================================
    // TC1 - DASHBOARD
    // =============================================================

    @Test
    @DisplayName("TC1 - Dashboard tra ve thanh cong")
    void tc1_Dashboard_Success() throws Exception {
        mockMvc.perform(get("/api/receptionist/dashboard"))
                .andExpect(status().isOk());
    }
    // =============================================================
    // TC2 - GET DOCTOR BY ID - SUCCESS
    // =============================================================

    @Test
    @DisplayName("TC2 - Get doctor by ID thanh cong")
    void tc2_GetDoctorById_Success() throws Exception {
        Doctor doctor = new Doctor();
        doctor.setId(1L);
        doctor.setName("Doctor A");

        when(receptionistService.getDoctorById(1L)).thenReturn(doctor);

        mockMvc.perform(get("/api/receptionist/doctor/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Doctor A"));
    }

    // =============================================================
    // TC3 - GET DOCTOR BY ID - NOT FOUND
    // =============================================================

    @Test
    @DisplayName("TC3 - Get doctor by ID khong ton tai -> 404")
    void tc3_GetDoctorById_NotFound() throws Exception {
        when(receptionistService.getDoctorById(999L))
                .thenThrow(new NoSuchElementException("No doctor found with ID 999"));

        mockMvc.perform(get("/api/receptionist/doctor/999"))
                .andExpect(status().isNotFound());
    }

    // =============================================================
    // TC4 - GET PATIENT BY ID - SUCCESS
    // =============================================================

    @Test
    @DisplayName("TC4 - Get patient by ID thanh cong")
    void tc4_GetPatientById_Success() throws Exception {
        Patient patient = new Patient();
        patient.setPatient_id(1L);
        patient.setFull_name("Patient A");

        when(receptionistService.getPatientById(1L)).thenReturn(patient);

        mockMvc.perform(get("/api/receptionist/patients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patient_id").value(1))
                .andExpect(jsonPath("$.full_name").value("Patient A"));
    }

    // =============================================================
    // TC5 - GET PATIENT BY ID - NOT FOUND
    // =============================================================

    @Test
    @DisplayName("TC5 - Get patient by ID khong ton tai -> null")
    void tc5_GetPatientById_NotFound() throws Exception {
        when(receptionistService.getPatientById(999L)).thenReturn(null);

        mockMvc.perform(get("/api/receptionist/patients/999"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    // =============================================================
    // TC6 - URL KHONG CO ID
    // =============================================================

    @Test
    @DisplayName("TC6 - URL khong co ID -> 404")
    void tc6_GetPatientById_NullId() throws Exception {

        mockMvc.perform(get("/api/receptionist/patients/"))
                .andExpect(status().isNotFound());
    }

    // =============================================================
    // TC7 - GET PATIENT BY ID - INVALID ID
    // =============================================================

    @Test
    @DisplayName("TC7 - Get patient by ID invalid -> null")
    void tc7_GetPatientById_InvalidId() throws Exception {
        when(receptionistService.getPatientById(-1L)).thenReturn(null);

        mockMvc.perform(get("/api/receptionist/patients/-1"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }
}