package com.aidims.aidimsbackend.service;

import com.aidims.aidimsbackend.entity.Assignment;
import com.aidims.aidimsbackend.entity.Doctor;
import com.aidims.aidimsbackend.entity.Patient;
import com.aidims.aidimsbackend.repository.AssignmentRepository;
import com.aidims.aidimsbackend.repository.DoctorRepository;
import com.aidims.aidimsbackend.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AssignmentService - Unit Tests")
class AssignmentServiceTest {

    @Mock
    private AssignmentRepository assignmentRepo;

    @Mock
    private PatientRepository patientRepo;

    @Mock
    private DoctorRepository doctorRepo;

    @InjectMocks
    private AssignmentService assignmentService;

    private Patient samplePatient;
    private Doctor sampleDoctor;
    private Assignment sampleAssignment;

    @BeforeEach
    void setUp() {
        samplePatient = new Patient();
        samplePatient.setPatient_id(1L);
        samplePatient.setFull_name("Patient A");

        sampleDoctor = new Doctor();
        sampleDoctor.setId(2L);
        sampleDoctor.setName("Doctor B");
        sampleDoctor.setDepartment("Cardiology");

        sampleAssignment = new Assignment();
        sampleAssignment.setId(10L);
        sampleAssignment.setPatient(samplePatient);
        sampleAssignment.setDoctor(sampleDoctor);
        sampleAssignment.setDepartment("Cardiology");
        sampleAssignment.setStatus("Đang chờ");
    }

    @Test
    @DisplayName("✅ assignDoctor - Phân công bác sĩ điều trị thành công")
    void assignDoctor_Success() {
        when(patientRepo.findById(1L)).thenReturn(Optional.of(samplePatient));
        when(doctorRepo.findById(2L)).thenReturn(Optional.of(sampleDoctor));
        when(assignmentRepo.save(any(Assignment.class))).thenAnswer(inv -> inv.getArgument(0));

        Assignment result = assignmentService.assignDoctor(1L, 2L, "Cardiology");

        assertNotNull(result);
        assertEquals(samplePatient, result.getPatient());
        assertEquals(sampleDoctor, result.getDoctor());
        assertEquals("Cardiology", result.getDepartment());
        assertEquals("Đang chờ", result.getStatus());
        assertNotNull(result.getAssignedAt());
        verify(assignmentRepo, times(1)).save(any(Assignment.class));
    }

    @Test
    @DisplayName("❌ assignDoctor - Ném ngoại lệ khi không tìm thấy bệnh nhân")
    void assignDoctor_PatientNotFound() {
        when(patientRepo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> 
            assignmentService.assignDoctor(999L, 2L, "Cardiology")
        );
        verify(assignmentRepo, never()).save(any(Assignment.class));
    }

    @Test
    @DisplayName("❌ assignDoctor - Ném ngoại lệ khi không tìm thấy bác sĩ")
    void assignDoctor_DoctorNotFound() {
        when(patientRepo.findById(1L)).thenReturn(Optional.of(samplePatient));
        when(doctorRepo.findById(888L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> 
            assignmentService.assignDoctor(1L, 888L, "Cardiology")
        );
        verify(assignmentRepo, never()).save(any(Assignment.class));
    }

    @Test
    @DisplayName("✅ getAllAssignments - Lấy danh sách toàn bộ phân công thành công")
    void getAllAssignments_Success() {
        when(assignmentRepo.findAll()).thenReturn(Arrays.asList(sampleAssignment));

        List<Assignment> result = assignmentService.getAllAssignments();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Cardiology", result.get(0).getDepartment());
    }

    @Test
    @DisplayName("❌ Lỗi nghiệp vụ: Không được phép phân công bác sĩ vào chuyên khoa khác với chuyên khoa của họ")
    void assignDoctor_DepartmentMismatch_ThrowsException() {
        when(patientRepo.findById(1L)).thenReturn(Optional.of(samplePatient));
        when(doctorRepo.findById(2L)).thenReturn(Optional.of(sampleDoctor));

        assertThrows(IllegalArgumentException.class, () -> 
            assignmentService.assignDoctor(1L, 2L, "Pediatrics"),
            "Chuyên khoa không khớp phải ném ra IllegalArgumentException"
        );
    }
}
