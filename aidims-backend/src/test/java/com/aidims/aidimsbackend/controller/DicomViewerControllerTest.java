package com.aidims.aidimsbackend.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    @DisplayName("✅ Serve file .dcm -> Content-Type image/jpeg")
    void testServeImage_DcmExtension() {
        when(dicomViewerService.getDicomViewerFilePath("sample.dcm")).thenReturn("dicom_uploads/sample.dcm");

        ResponseEntity<Resource> response = dicomViewerController.serveImageBrowserCompatible("sample.dcm");

        assertEquals(200, response.getStatusCode().value());
        assertEquals("image/jpeg", response.getHeaders().getContentType().toString());
    }

    @Test
    @DisplayName("⚠️ Cố tình ép lỗi -> Bẫy test trả về sai mã mong đợi để kích hoạt Bug lên Jira")
    void testGetAllDicomViewer_ExceptionHandling_ForceFailure() {
        when(dicomViewerService.getAllDicomViewer()).thenThrow(new RuntimeException("Fatal database failure"));

        ResponseEntity<List<Map<String, Object>>> response = dicomViewerController.getAllDicomViewer();

        assertEquals(200, response.getStatusCode().value(), "Kích hoạt báo động đỏ phân hệ Dicom Viewer!");
    }
}