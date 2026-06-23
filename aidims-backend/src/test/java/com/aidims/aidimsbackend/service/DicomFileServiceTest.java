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
        // 1. Tạo file gốc tạm thời
        Path sourceFile = Files.createTempFile("source", ".dcm");
        Files.writeString(sourceFile, "dicom-content", StandardCharsets.UTF_8);

        // 2. Đặt tên file đích độc nhất
        String targetFilename = "copied_test_" + System.currentTimeMillis() + ".dcm";
        
        // Chỉ định thẳng vào đường dẫn project frontend của bạn
        Path frontendDir = Paths.get("../aidims-frontend/public/dicom_uploads");
        Files.createDirectories(frontendDir); // Đảm bảo thư mục tồn tại
        
        Path targetFile = frontendDir.resolve(targetFilename);

        try {
            // 3. Thực thi
            dicomFileService.copyFileToFrontend(sourceFile.toFile(), targetFilename);

            // 4. Kiểm tra
            assertTrue(Files.exists(targetFile), "File chưa được copy tới: " + targetFile.toAbsolutePath());
            assertEquals("dicom-content", Files.readString(targetFile, StandardCharsets.UTF_8));
        } finally {
            // 5. Dọn dẹp rác (Cực kỳ quan trọng để ổ cứng không bị đầy)
            Files.deleteIfExists(sourceFile);
            Files.deleteIfExists(targetFile);
        }
    }

    @Test
    @DisplayName("✅ saveAndCopyToFrontend lưu ở backend và copy sang frontend")
    void saveAndCopyToFrontend_shouldSaveAndCopyFile() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("scan_test.dcm");

        // Dùng biến này để "tóm" lấy cái đường dẫn mà Service định lưu vào
        AtomicReference<File> savedBackendFile = new AtomicReference<>();

        // Giả lập hành vi khi Service gọi hàm lưu file
        doAnswer(invocation -> {
            File targetFile = invocation.getArgument(0);
            savedBackendFile.set(targetFile); // Tóm lấy file
            
            // Ghi nội dung giả vào file backend để hàm sau có nội dung để copy sang frontend
            Files.createDirectories(targetFile.toPath().getParent());
            Files.writeString(targetFile.toPath(), "dicom-content", StandardCharsets.UTF_8);
            return null;
        }).when(file).transferTo(any(File.class));

        // Thực thi
        dicomFileService.saveAndCopyToFrontend(file);

        // Kiểm tra Backend File
        File backendFile = savedBackendFile.get();
        assertNotNull(backendFile, "Hàm transferTo chưa được gọi để lưu file ở backend");

        Path backendPath = backendFile.toPath();
        String generatedFilename = backendFile.getName(); // Lấy tên file có chứa timestamp

        // Xác định đường dẫn Frontend thực tế dựa vào tên file vừa tạo
        Path frontendPath = Paths.get("../aidims-frontend/public/dicom_uploads").resolve(generatedFilename);

        try {
            assertTrue(Files.exists(backendPath), "File backend không tồn tại");
            assertTrue(Files.exists(frontendPath), "File frontend không tồn tại");
            
            assertEquals("dicom-content", Files.readString(backendPath, StandardCharsets.UTF_8));
            assertEquals("dicom-content", Files.readString(frontendPath, StandardCharsets.UTF_8));
        } finally {
            // Dọn rác
            Files.deleteIfExists(backendPath);
            Files.deleteIfExists(frontendPath);
        }
    }
    @Test
    @DisplayName("❌ Lỗi nghiệp vụ (Bug-CI): Chặn upload file rỗng (0 bytes)")
    void saveAndCopyToFrontend_WithEmptyFile_ShouldThrowException() throws Exception {
        // 1. Giả lập một file được upload lên nhưng bị RỖNG (size = 0)
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true); 
        when(file.getOriginalFilename()).thenReturn("file_rac.dcm");

        // 2. ĐẶT BẪY: Ép Service phải văng lỗi IllegalArgumentException.
        // Chắc chắn Dev chưa viết if(file.isEmpty()) chặn lại đâu, nên Test này sẽ ĐỎ RỰC!
        assertThrows(IllegalArgumentException.class, () -> {
            dicomFileService.saveAndCopyToFrontend(file);
        }, "Hệ thống bị lủng! Cho phép upload file rỗng (0 bytes) làm đầy ổ cứng!");
    }
}