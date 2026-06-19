package com.aidims.aidimsbackend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aidims.aidimsbackend.entity.Assignment;
import com.aidims.aidimsbackend.entity.Doctor;
import com.aidims.aidimsbackend.entity.Patient;
import com.aidims.aidimsbackend.entity.Symptom;
import com.aidims.aidimsbackend.repository.AssignmentRepository;
import com.aidims.aidimsbackend.repository.DoctorRepository;
import com.aidims.aidimsbackend.repository.PatientRepository;
import com.aidims.aidimsbackend.repository.SymptomRepository;

@Service
public class ReceptionistService {
    @Autowired private PatientRepository patientRepo;
    @Autowired private SymptomRepository symptomRepo;
    @Autowired private AssignmentRepository assignmentRepo;
    @Autowired private DoctorRepository doctorRepo;

    public Symptom recordSymptom(Long patientId, String description) {
        // Validation: Mô tả không được rỗng
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Mô tả triệu chứng không được để trống!");
        }
        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bệnh nhân với ID: " + patientId));
        
        Symptom symptom = new Symptom();
        symptom.setPatient(patient);
        symptom.setDescription(description);
        symptom.setRecordedAt(LocalDateTime.now());
        return symptomRepo.save(symptom);
    }

    public Assignment assignDoctor(Long patientId, Long doctorId, String department, String priority, String notes) {
        // Validation: Phòng ban không được rỗng
        if (department == null || department.trim().isEmpty()) {
            throw new IllegalArgumentException("Phòng ban không được để trống!");
        }
        
        // Validation: Độ ưu tiên không được rỗng VÀ phải đúng định dạng
        if (priority == null || priority.trim().isEmpty() || 
            (!priority.equalsIgnoreCase("Cao") && 
             !priority.equalsIgnoreCase("Trung bình") && 
             !priority.equalsIgnoreCase("Thấp"))) {
            throw new IllegalArgumentException("Độ ưu tiên không hợp lệ!");
        }

        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bệnh nhân ID: " + patientId));
        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bác sĩ ID: " + doctorId));
        
        Assignment a = new Assignment();
        a.setPatient(patient);
        a.setDoctor(doctor);
        a.setDepartment(department);
        a.setAssignedAt(LocalDateTime.now());
        a.setStatus("Đang chờ");
        a.setPriority(priority);
        a.setNotes(notes);
        return assignmentRepo.save(a);
    }

    public List<Assignment> getAllAssignments() {
        return assignmentRepo.findAll();
    }

    public List<Patient> getAllPatients() {
        return patientRepo.findAll();
    }

    public Patient createOrUpdatePatient(Patient patient) {
        // Validation: Patient không được null
        if (patient == null) throw new IllegalArgumentException("Thông tin bệnh nhân không hợp lệ!");

        if (patient.getDate_of_birth() != null && !patient.getDate_of_birth().isEmpty()) {
            LocalDate dob = LocalDate.parse(patient.getDate_of_birth());
            
            // Validation: Ngày sinh không được ở tương lai
            if (dob.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("Ngày sinh không thể ở tương lai!");
            }
            
            int age = Period.between(dob, LocalDate.now()).getYears();
            // Validation: Tuổi không được âm
            if (age < 0) {
                throw new IllegalArgumentException("Tuổi không hợp lệ!");
            }
            patient.setAge(age);
        }
        return patientRepo.save(patient);
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepo.findAll();
    }

    public List<Doctor> getDoctorsByDepartment(String department) {
        return doctorRepo.findAll().stream()
            .filter(d -> d.getDepartment() != null && d.getDepartment().equalsIgnoreCase(department))
            .toList();
    }

    public Doctor getDoctorById(Long id) {
        return doctorRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bác sĩ!"));
    }

    public Patient getPatientById(Long id) {
        if (id == null) throw new IllegalArgumentException("ID bệnh nhân không được null!");
        return patientRepo.findById(id).orElse(null);
    }
}
