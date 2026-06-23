package com.aidims.aidimsbackend.service;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aidims.aidimsbackend.entity.DicomImport;
import com.aidims.aidimsbackend.repository.DicomImportRepository;

@Service
public class DicomImportService {
    
    @Autowired
    private DicomImportRepository dicomImportRepository;

    public DicomImport saveDicomImport(DicomImport dicomImport) {
        
        // --- 1. TƯỜNG LỬA CHẶN MÃ BỆNH NHÂN RỖNG ---
        if (dicomImport.getPatientCode() == null || dicomImport.getPatientCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã bệnh nhân không được để trống");
        }

        // --- 2. TƯỜNG LỬA CHẶN FILE KHÔNG PHẢI DICOM ---
        if (dicomImport.getFileName() == null || !dicomImport.getFileName().toLowerCase().endsWith(".dcm")) {
            throw new IllegalArgumentException("Chỉ chấp nhận file định dạng .dcm");
        }

        // Code cũ của bạn: Gán thời gian và lưu Database
        dicomImport.setImportDate(new Timestamp(System.currentTimeMillis()));
        return dicomImportRepository.save(dicomImport);
    }
}