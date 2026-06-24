package com.aidims.aidimsbackend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

@DisplayName("DicomFileService - Unit Tests (Real Path Check)")
class DicomFileServiceTest {

    private final DicomFileService dicomFileService = new DicomFileService();

    @Test
    @DisplayName("✅ copyFileToFrontend sao chép file sang thư mục frontend thực tế")
    void copyFileToFrontend_shouldCopyFileToFrontendDirectory() throws Exception {
        Path sourceFile = Files.createTempFile("source", ".dcm");
        Files.writeString(sourceFile, "dicom-content", StandardCharsets.UTF_8);

        String targetFilename = "copied_test_" + System.currentTimeMillis() + ".dcm";
        
        Path frontendDir = Paths.get("../aidims-frontend/public/dicom_uploads");
        Files.createDirectories(frontendDir);
        
        Path targetFile = frontendDir.resolve(targetFilename);

        try {
            dicomFileService.copyFileToFrontend(sourceFile.toFile(), targetFilename);

            assertTrue(Files.exists(targetFile), "File chưa được copy tới: " + targetFile.toAbsolutePath());
            assertEquals("dicom-content", Files.readString(targetFile, StandardCharsets.UTF_8));
        } finally {
            Files.deleteIfExists(sourceFile);
            Files.deleteIfExists(targetFile);
        }
    }

    @Test
    @DisplayName("✅ saveAndCopyToFrontend lưu ở backend và copy sang frontend")
    void saveAndCopyToFrontend_shouldSaveAndCopyFile() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("scan_test.dcm");

        AtomicReference<File> savedBackendFile = new AtomicReference<>();

        doAnswer(invocation -> {
            File targetFile = invocation.getArgument(0);
            savedBackendFile.set(targetFile);
            
            Files.createDirectories(targetFile.toPath().getParent());
            Files.writeString(targetFile.toPath(), "dicom-content", StandardCharsets.UTF_8);
            return null;
        }).when(file).transferTo(any(File.class));

        dicomFileService.saveAndCopyToFrontend(file);

        File backendFile = savedBackendFile.get();
        assertNotNull(backendFile, "Hàm transferTo chưa được gọi để lưu file ở backend");

        Path backendPath = backendFile.toPath();
        String generatedFilename = backendFile.getName();

        Path frontendPath = Paths.get("../aidims-frontend/public/dicom_uploads").resolve(generatedFilename);

        try {
            assertTrue(Files.exists(backendPath), "File backend không tồn tại");
            assertTrue(Files.exists(frontendPath), "File frontend không tồn tại");
            
            assertEquals("dicom-content", Files.readString(backendPath, StandardCharsets.UTF_8));
            assertEquals("dicom-content", Files.readString(frontendPath, StandardCharsets.UTF_8));
        } finally {
            Files.deleteIfExists(backendPath);
            Files.deleteIfExists(frontendPath);
        }
    }
    @Test
    @DisplayName("❌ Lỗi nghiệp vụ (Bug-CI): Chặn upload file rỗng (0 bytes)")
    void saveAndCopyToFrontend_WithEmptyFile_ShouldThrowException() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true); 
        when(file.getOriginalFilename()).thenReturn("file_rac.dcm");

        assertThrows(IllegalArgumentException.class, () -> {
            dicomFileService.saveAndCopyToFrontend(file);
        }, "Hệ thống bị lủng! Cho phép upload file rỗng (0 bytes) làm đầy ổ cứng!");
    }
}