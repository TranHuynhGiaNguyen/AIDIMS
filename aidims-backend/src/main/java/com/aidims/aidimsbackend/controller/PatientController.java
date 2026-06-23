package com.aidims.aidimsbackend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aidims.aidimsbackend.entity.Patient;
import com.aidims.aidimsbackend.repository.PatientRepository;

@RestController
@RequestMapping("/api/patients")
@CrossOrigin(origins = "http://localhost:3000")
public class PatientController {

    @Autowired
    private PatientRepository patientRepository;

    @GetMapping
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    @GetMapping("/{id}")
    public Patient getPatientById(@PathVariable Long id) {
        Patient patient = patientRepository.findById(id).orElse(null);
        if (patient != null && patient.getOxygen_saturation() != null) {
            Integer spo2 = patient.getOxygen_saturation();
            if (spo2 > 100) {
                patient.setOxygen_saturation(100);
            } else if (spo2 < 0) {
                patient.setOxygen_saturation(0);
            }
        }
        return patient;
    }
}