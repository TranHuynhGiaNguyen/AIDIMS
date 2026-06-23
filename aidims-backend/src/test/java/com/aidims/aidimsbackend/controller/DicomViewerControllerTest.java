package com.aidims.aidimsbackend.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import com.aidims.aidimsbackend.service.DicomViewerService;

@ExtendWith(MockitoExtension.class)
@DisplayName("DicomViewerControllerTest - TASK-37 Operations")
class DicomViewerControllerTest {

    @Mock
    private DicomViewerService dicomViewerService;

    @InjectMocks
    private DicomViewerController dicomViewerController;

    private List<Map<String, Object>> mockDicomList;
    private Map<String, Object> mockStats;

    @BeforeEach
    void setUp() {
        mockDicomList = new ArrayList<>();
        Map<String, Object> record = new HashMap<>();
        record.put("id", 1L);
        record.put("fileName", "sample.dcm");
        record.put("patientCode", "BN_POSTMAN");
        mockDicomList.add(record);

        mockStats = new HashMap<>();
        mockStats.put("totalCount", 10);
    }

    // =========================================================
    // 1. BASIC API OPERATION TESTS
    // =========================================================

    @Test
    @DisplayName("✅ Lấy tất cả DICOM records thành công")
    void testGetAllDicomViewer_Success() {
        when(dicomViewerService.getAllDicomViewer()).thenReturn(mockDicomList);

        ResponseEntity<List<Map<String, Object>>> response = dicomViewerController.getAllDicomViewer();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        verify(dicomViewerService, times(1)).getAllDicomViewer();
    }

    @Test
    @DisplayName("✅ Lấy DICOM theo ID thành công")
    void testGetDicomViewerById_Found() {
        when(dicomViewerService.getDicomViewerById(1L)).thenReturn(mockDicomList.get(0));

        ResponseEntity<Map<String, Object>> response = dicomViewerController.getDicomViewerById(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("sample.dcm", response.getBody().get("fileName"));
    }

    @Test
    @DisplayName("❌ Lấy DICOM theo ID không tìm thấy -> Trả về 404")
    void testGetDicomViewerById_NotFound() {
        when(dicomViewerService.getDicomViewerById(99L)).thenReturn(null);

        ResponseEntity<Map<String, Object>> response = dicomViewerController.getDicomViewerById(99L);

        assertEquals(404, response.getStatusCode().value());
    }

    // =========================================================
    // 2. SERVE IMAGE ENDPOINT CHECK
    // =========================================================

    @Test
    @DisplayName("✅ Serve file .dcm -> Content-Type image/jpeg")
    void testServeImage_DcmExtension() {
        when(dicomViewerService.getDicomViewerFilePath("sample.dcm")).thenReturn("dicom_uploads/sample.dcm");

        ResponseEntity<Resource> response = dicomViewerController.serveImageBrowserCompatible("sample.dcm");

        assertEquals(200, response.getStatusCode().value());
        assertEquals("image/jpeg", response.getHeaders().getContentType().toString());
    }

    // =========================================================
    // 3. FORCE FAILURE TO TRIGGER JIRA BUG AUTOMATICALLY
    // =========================================================

    @Test
    @DisplayName("❌ Lỗi nghiệp vụ: Dữ liệu ngày chụp trả về từ API /all phải tuân thủ định dạng dd/MM/yyyy HH:mm")
    void testGetAllDicomViewer_DateFormatValidation_LogicFailure() {
        List<Map<String, Object>> badFormatList = new ArrayList<>();
        Map<String, Object> record = new HashMap<>();
        record.put("id", 1L);
        record.put("fileName", "sample.dcm");
        record.put("dateTaken", "2026-06-24 11:00:00"); // Định dạng sai logic (phải là dd/MM/yyyy HH:mm)
        badFormatList.add(record);

        when(dicomViewerService.getAllDicomViewer()).thenReturn(badFormatList);

        ResponseEntity<List<Map<String, Object>>> response = dicomViewerController.getAllDicomViewer();
        List<Map<String, Object>> body = response.getBody();

        assertNotNull(body);
        assertEquals(1, body.size());

        // Kiểm định logic định dạng ngày.
        // Test case này sẽ FAIL về mặt logic nghiệp vụ (mặc dù HTTP status trả về vẫn là 200 OK)
        String dateTaken = (String) body.get(0).get("dateTaken");
        assertTrue(dateTaken.matches("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}"), 
                   "Định dạng ngày chụp không hợp lệ, phải là dd/MM/yyyy HH:mm nhưng nhận được: " + dateTaken);
    }
}