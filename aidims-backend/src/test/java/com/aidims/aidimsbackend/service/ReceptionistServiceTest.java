package com.aidims.aidimsbackend.service;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import com.aidims.aidimsbackend.entity.Doctor;
import com.aidims.aidimsbackend.entity.Patient;
import com.aidims.aidimsbackend.repository.AssignmentRepository;
import com.aidims.aidimsbackend.repository.DoctorRepository;
import com.aidims.aidimsbackend.repository.PatientRepository;
import com.aidims.aidimsbackend.repository.SymptomRepository;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ReceptionistService - Bug Detection Tests")
class ReceptionistServiceTest {

    @Mock
    private PatientRepository patientRepo;

    @Mock
    private SymptomRepository symptomRepo;

    @Mock
    private AssignmentRepository assignmentRepo;

    @Mock
    private DoctorRepository doctorRepo;

    @InjectMocks
    private ReceptionistService receptionistService;

    @Nested
    @DisplayName("Record Symptom Validation")
    class SymptomTests {

        @Test
        @DisplayName("Empty symptom description should be rejected")
        void testEmptyDescription() {

            Patient patient = new Patient();

            when(patientRepo.findById(1L))
                    .thenReturn(Optional.of(patient));

            assertThrows(
                    IllegalArgumentException.class,
                    () -> receptionistService.recordSymptom(1L, "")
            );
        }

        @Test
        @DisplayName("Null symptom description should be rejected")
        void testNullDescription() {

            Patient patient = new Patient();

            when(patientRepo.findById(1L))
                    .thenReturn(Optional.of(patient));

            assertThrows(
                    IllegalArgumentException.class,
                    () -> receptionistService.recordSymptom(1L, null)
            );
        }

        @Test
        @DisplayName("Non-existing patient should not record symptom")
        void testPatientNotFound() {

            when(patientRepo.findById(999L))
                    .thenReturn(Optional.empty());

            assertThrows(
                    RuntimeException.class,
                    () -> receptionistService.recordSymptom(999L, "Headache")
            );
        }
    }

    @Nested
    @DisplayName("Assign Doctor Validation")
    class AssignDoctorTests {

        @Test
        @DisplayName("Empty department should be rejected")
        void testEmptyDepartment() {

            Patient patient = new Patient();
            Doctor doctor = new Doctor();

            when(patientRepo.findById(1L))
                    .thenReturn(Optional.of(patient));

            when(doctorRepo.findById(1L))
                    .thenReturn(Optional.of(doctor));

            assertThrows(
                    IllegalArgumentException.class,
                    () -> receptionistService.assignDoctor(
                            1L,
                            1L,
                            "",
                            "HIGH",
                            "test"
                    )
            );
        }

        @Test
        @DisplayName("Invalid priority should be rejected")
        void testInvalidPriority() {

            Patient patient = new Patient();
            Doctor doctor = new Doctor();

            when(patientRepo.findById(1L))
                    .thenReturn(Optional.of(patient));

            when(doctorRepo.findById(1L))
                    .thenReturn(Optional.of(doctor));

            assertThrows(
                    IllegalArgumentException.class,
                    () -> receptionistService.assignDoctor(
                            1L,
                            1L,
                            "Cardiology",
                            "SUPER-URGENT",
                            "test"
                    )
            );
        }

        @Test
        @DisplayName("Doctor not found")
        void testDoctorNotFound() {

            Patient patient = new Patient();

            when(patientRepo.findById(1L))
                    .thenReturn(Optional.of(patient));

            when(doctorRepo.findById(999L))
                    .thenReturn(Optional.empty());

            assertThrows(
                    RuntimeException.class,
                    () -> receptionistService.assignDoctor(
                            1L,
                            999L,
                            "Cardiology",
                            "HIGH",
                            "test"
                    )
            );
        }
    }

    @Nested
    @DisplayName("Create Patient Validation")
    class PatientValidationTests {

        @Test
        @DisplayName("Future date of birth should be rejected")
        void testFutureDateOfBirth() {

            Patient patient = new Patient();

            patient.setDate_of_birth(
                    LocalDate.now().plusYears(1).toString()
            );

            assertThrows(
                    IllegalArgumentException.class,
                    () -> receptionistService.createOrUpdatePatient(patient)
            );
        }

        @Test
        @DisplayName("Age cannot be negative")
        void testNegativeAgeBug() {

            Patient patient = new Patient();

            patient.setDate_of_birth("2100-01-01");

            assertThrows(
                    IllegalArgumentException.class,
                    () -> receptionistService.createOrUpdatePatient(patient)
            );
        }

        @Test
        @DisplayName("Invalid date format should be rejected")
        void testInvalidDateFormat() {

            Patient patient = new Patient();

            patient.setDate_of_birth("32-15-2025");

            assertThrows(
                    RuntimeException.class,
                    () -> receptionistService.createOrUpdatePatient(patient)
            );
        }
    }

    @Nested
    @DisplayName("Repository Safety")
    class RepositoryTests {

        @Test
        @DisplayName("Invalid patient should not be saved")
        void testInvalidPatientNotSaved() {

            Patient patient = new Patient();

            patient.setDate_of_birth("2100-01-01");

            try {
                receptionistService.createOrUpdatePatient(patient);
            } catch (Exception ignored) {}

            verify(patientRepo, never())
                    .save(any(Patient.class));
        }
    }
}