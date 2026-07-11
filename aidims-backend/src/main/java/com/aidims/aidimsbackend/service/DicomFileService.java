package com.aidims.aidimsbackend.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DicomFileService {

    private Path getFrontendDir() {
        String userDir = System.getProperty("user.dir");
        if (userDir.endsWith("aidims-backend") || userDir.endsWith("aidims-backend\\") || userDir.endsWith("aidims-backend/")) {
            return Paths.get(userDir).getParent().resolve("aidims-frontend/public/dicom_uploads");
        } else {
            return Paths.get(userDir).resolve("aidims-frontend/public/dicom_uploads");
        }
    }

    private Path getBackendDir() {
        String userDir = System.getProperty("user.dir");
        if (userDir.endsWith("aidims-backend") || userDir.endsWith("aidims-backend\\") || userDir.endsWith("aidims-backend/")) {
            return Paths.get(userDir).resolve("dicom_uploads");
        } else {
            return Paths.get(userDir).resolve("aidims-backend/dicom_uploads");
        }
    }

    public void saveAndCopyToFrontend(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Hệ thống từ chối nhận file rỗng (0 bytes)");
        }
        // Lưu vào backend
        Path backendDir = getBackendDir();
        Files.createDirectories(backendDir);
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path backendPath = backendDir.resolve(fileName);
        file.transferTo(backendPath.toFile());

        // Copy sang frontend/public/dicom_uploads
        Path frontendDir = getFrontendDir();
        Files.createDirectories(frontendDir);
        Path frontendPath = frontendDir.resolve(fileName);
        Files.copy(backendPath, frontendPath, StandardCopyOption.REPLACE_EXISTING);

        System.out.println("Đã copy sang frontend: " + frontendPath.toAbsolutePath());
    }

    public void copyFileToFrontend(File sourceFile, String fileName) throws IOException {
        Path frontendDir = getFrontendDir();
        Files.createDirectories(frontendDir);
        Path frontendPath = frontendDir.resolve(fileName);
        Files.copy(sourceFile.toPath(), frontendPath, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Đã copy sang frontend: " + frontendPath.toAbsolutePath());
    }
}