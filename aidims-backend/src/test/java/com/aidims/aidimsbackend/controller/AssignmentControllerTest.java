package com.aidims.aidimsbackend.controller;

import com.aidims.aidimsbackend.entity.Assignment;
import com.aidims.aidimsbackend.entity.Doctor;
import com.aidims.aidimsbackend.entity.Patient;
import com.aidims.aidimsbackend.service.ReceptionistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AssignmentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReceptionistService receptionistService;

    @InjectMocks
    private AssignmentController assignmentController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(assignmentController).build();
    }

    @Test
    void assignDoctor_withMismatchedDepartment_shouldReturnBadRequest() throws Exception {
        Doctor doctor = new Doctor();
        doctor.setId(2L);
        doctor.setName("Bác sĩ A");
        doctor.setDepartment("Nội khoa"); // Chuyên khoa thực tế của bác sĩ

        Patient patient = new Patient();
        patient.setPatient_id(1L);

        // Giả lập gán bác sĩ cho khoa "Ngoại khoa" (khác với chuyên khoa "Nội khoa" của
        // bác sĩ)
        // Nếu hệ thống kiểm tra logic này, nó phải ném lỗi hoặc trả về Bad Request.
        // Nhưng vì không kiểm tra nên nó vẫn lưu và trả về Assignment thành công (200
        // OK).
        Assignment assignment = new Assignment();
        assignment.setDoctor(doctor);
        assignment.setPatient(patient);
        assignment.setDepartment("Ngoại khoa"); // Khoa chỉ định khác khoa bác sĩ

        Mockito.when(receptionistService.assignDoctor(anyLong(), anyLong(), anyString(), any(), any()))
                .thenReturn(assignment);

        mockMvc.perform(post("/api/receptionist/assign")
                .param("patientId", "1")
                .param("doctorId", "2")
                .param("department", "Ngoại khoa"))
                .andExpect(status().isBadRequest()); // Sẽ thất bại (ra bug) do hệ thống trả về 200 OK
    }
}
