package com.aidims.aidimsbackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DicomViewerService - Unit Tests")
class DicomViewerServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private DicomViewerService dicomViewerService;

    private Map<String, Object> sampleDicomRow;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(dicomViewerService, "serverPort", "8080");

        sampleDicomRow = new HashMap<>();
        sampleDicomRow.put("id", 1L);
        sampleDicomRow.put("file_name", "test_file.dcm");
        sampleDicomRow.put("file_path", "dicom_uploads/test_file.dcm");
        sampleDicomRow.put("file_size", 2048L);
        sampleDicomRow.put("import_date", LocalDateTime.of(2026, 6, 24, 11, 0));
        sampleDicomRow.put("notes", "My notes");
        sampleDicomRow.put("patient_code", "BN001");
        sampleDicomRow.put("performed_by", 5L);
        sampleDicomRow.put("status", "imported");
        sampleDicomRow.put("study_type", "CT");
        sampleDicomRow.put("technical_params", "{\"kVp\":\"120\"}");
        sampleDicomRow.put("patient_name", "John Smith");
        sampleDicomRow.put("gender", "Male");
        sampleDicomRow.put("patient_phone", "0123456789");
        sampleDicomRow.put("performed_by_name", "Dr. Alice");
    }

    @Test
    @DisplayName("✅ getAllDicomViewer - Trả về danh sách DICOM đã chuẩn hóa thành công")
    void getAllDicomViewer_Success() {
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(sampleDicomRow);

        when(jdbcTemplate.queryForList(anyString())).thenReturn(rows);

        List<Map<String, Object>> result = dicomViewerService.getAllDicomViewer();

        assertNotNull(result);
        assertEquals(1, result.size());
        Map<String, Object> record = result.get(0);
        assertEquals("test_file.dcm", record.get("fileName"));
        assertEquals("John Smith", record.get("fullName"));
        assertEquals("http://localhost:8080/api/dicom-viewer/image/test_file.dcm", record.get("imageUrl"));
    }

    @Test
    @DisplayName("✅ getDicomViewerById - Lấy chi tiết DICOM theo ID thành công")
    void getDicomViewerById_Found() {
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(sampleDicomRow);

        when(jdbcTemplate.queryForList(anyString(), eq(1L))).thenReturn(rows);

        Map<String, Object> result = dicomViewerService.getDicomViewerById(1L);

        assertNotNull(result);
        assertEquals("test_file.dcm", result.get("fileName"));
        assertEquals("BN001", result.get("patientCode"));
    }

    @Test
    @DisplayName("✅ getDicomViewerById - Trả về null khi không tìm thấy ID")
    void getDicomViewerById_NotFound() {
        when(jdbcTemplate.queryForList(anyString(), eq(999L))).thenReturn(new ArrayList<>());

        Map<String, Object> result = dicomViewerService.getDicomViewerById(999L);

        assertNull(result);
    }

    @Test
    @DisplayName("✅ getDicomViewerByPatient - Lấy danh sách DICOM của bệnh nhân thành công")
    void getDicomViewerByPatient_Success() {
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(sampleDicomRow);

        when(jdbcTemplate.queryForList(anyString(), eq("BN001"))).thenReturn(rows);

        List<Map<String, Object>> result = dicomViewerService.getDicomViewerByPatient("BN001");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("BN001", result.get(0).get("patientCode"));
    }

    @Test
    @DisplayName("✅ searchDicomViewer - Tìm kiếm DICOM theo từ khóa thành công")
    void searchDicomViewer_Success() {
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(sampleDicomRow);

        String keyword = "John";
        String pattern = "%" + keyword + "%";

        when(jdbcTemplate.queryForList(anyString(),
                eq(pattern), eq(pattern), eq(pattern),
                eq(pattern), eq(pattern), eq(pattern)))
                .thenReturn(rows);

        List<Map<String, Object>> result = dicomViewerService.searchDicomViewer(keyword);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("✅ getDicomViewerFilePath - Lấy đường dẫn tệp tin DICOM lưu trong DB thành công")
    void getDicomViewerFilePath_Success() {
        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put("file_path", "dicom_uploads/test_file.dcm");
        rows.add(row);

        when(jdbcTemplate.queryForList(anyString(), eq("test_file.dcm"))).thenReturn(rows);

        String path = dicomViewerService.getDicomViewerFilePath("test_file.dcm");

        assertEquals("dicom_uploads/test_file.dcm", path);
    }

    @Test
    @DisplayName("✅ getDicomViewerStats - Thống kê chính xác số liệu lưu trữ DICOM")
    void getDicomViewerStats_Success() {
        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, Object> statsRow = new HashMap<>();
        statsRow.put("total_count", 15L);
        statsRow.put("mri_count", 5L);
        statsRow.put("ct_count", 5L);
        statsRow.put("xray_count", 5L);
        statsRow.put("total_size", 102400L);
        statsRow.put("unique_patients", 3L);
        rows.add(statsRow);

        when(jdbcTemplate.queryForList(anyString())).thenReturn(rows);

        Map<String, Object> stats = dicomViewerService.getDicomViewerStats();

        assertNotNull(stats);
        assertEquals(15L, stats.get("total_count"));
        assertEquals(5L, stats.get("mri_count"));
        assertEquals(102400L, stats.get("total_size"));
    }

    @Test
    @DisplayName("❌ Lỗi nghiệp vụ: Đường dẫn imageUrl của DICOM Viewer chứa khoảng trắng phải được mã hóa URL thành %20 hoặc phù hợp")
    void getAllDicomViewer_shouldUrlEncodeImageUrls() {
        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, Object> row = new HashMap<>(sampleDicomRow);
        row.put("file_name", "chest scan.dcm");
        rows.add(row);

        when(jdbcTemplate.queryForList(anyString())).thenReturn(rows);

        List<Map<String, Object>> result = dicomViewerService.getAllDicomViewer();
        String imageUrl = (String) result.get(0).get("imageUrl");

        assertNotNull(imageUrl);
        assertTrue(imageUrl.contains("chest%20scan.dcm"), "Đường dẫn ảnh phải được URL encode: " + imageUrl);
    }
}
