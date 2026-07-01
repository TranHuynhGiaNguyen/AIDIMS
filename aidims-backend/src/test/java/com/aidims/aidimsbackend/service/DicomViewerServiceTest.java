package com.aidims.aidimsbackend.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import com.aidims.aidimsbackend.repository.ImagingResultRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("DicomViewerServiceTest - Kiểm thử Hộp trắng lớp Dịch vụ Nghiệp vụ")
class DicomViewerServiceTest {

    @Mock
    private ImagingResultRepository imagingResultRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private DicomViewerService dicomViewerService;

    private Map<String, Object> dbRow;

    @BeforeEach
    void setUp() {
        // Thiết lập giá trị cổng server port giả lập qua Reflection để phục vụ hàm sinh Url ảnh
        ReflectionTestUtils.setField(dicomViewerService, "serverPort", "8080");

        // Giả lập cấu trúc dữ liệu thô (Row) trả về từ Database của bảng dicom_imports
        dbRow = new HashMap<>();
        dbRow.put("id", 1L);
        dbRow.put("body_part", "CHEST");
        dbRow.put("file_name", "chest_xray.dcm");
        dbRow.put("file_path", "dicom_uploads/chest_xray.dcm");
        dbRow.put("patient_code", "BN999");
        dbRow.put("patient_name", "Bệnh Nhân Nguyễn Văn A");
        dbRow.put("created_at", java.sql.Timestamp.valueOf("2026-07-01 10:30:00"));
    }

    @Test
    @DisplayName("✅ Lấy tất cả danh sách ảnh DICOM Viewer thành công và định dạng Url chuẩn")
    void testGetAllDicomViewer_Success() {
        List<Map<String, Object>> mockDbResults = new ArrayList<>();
        mockDbResults.add(dbRow);

        // Giả lập JdbcTemplate trả về kết quả thô khi thực thi câu lệnh SQL native
        when(jdbcTemplate.queryForList(anyString())).thenReturn(mockDbResults);

        // Thực thi hàm nghiệp vụ dịch vụ cần kiểm thử
        List<Map<String, Object>> result = dicomViewerService.getAllDicomViewer();

        // Khẳng định đầu ra (Assertions)
        assertNotNull(result);
        assertEquals(1, result.size());
        
        Map<String, Object> transformedRecord = result.get(0);
        assertEquals("Bệnh Nhân Nguyễn Văn A", transformedRecord.get("fullName"));
        assertEquals("01/07/2026 10:30", transformedRecord.get("createdAt"));
        assertEquals("http://localhost:8080/api/dicom-viewer/image/chest_xray.dcm", transformedRecord.get("imageUrl"));

        verify(jdbcTemplate, times(1)).queryForList(anyString());
    }

    @Test
    @DisplayName("✅ Truy xuất chi tiết ảnh DICOM theo ID thành công")
    void testGetDicomViewerById_Found() {
        // Giả lập luồng trả về một bản ghi duy nhất khi truyền đúng mã định danh ID
        when(jdbcTemplate.queryForMap(anyString(), eq(1L))).thenReturn(dbRow);

        Map<String, Object> result = dicomViewerService.getDicomViewerById(1L);

        assertNotNull(result);
        assertEquals(1L, result.get("id"));
        assertEquals("Bệnh Nhân Nguyễn Văn A", result.get("fullName"));
        assertEquals("http://localhost:8080/api/dicom-viewer/image/chest_xray.dcm", result.get("imageUrl"));
        
        verify(jdbcTemplate, times(1)).queryForMap(anyString(), eq(1L));
    }

    @Test
    @DisplayName("❌ Bản ghi khuyết thiếu tệp tin gốc -> imageUrl gán bằng null")
    void testGetDicomViewerById_MissingFileName() {
        // Kiểm thử rẽ nhánh (Branch Coverage) khi trường file_name trống hoặc null
        dbRow.put("file_name", ""); 
        when(jdbcTemplate.queryForMap(anyString(), eq(1L))).thenReturn(dbRow);

        Map<String, Object> result = dicomViewerService.getDicomViewerById(1L);

        assertNotNull(result);
        assertNull(result.get("imageUrl")); // Nhánh code logic gán url null hoạt động đúng chuẩn
    }

    @Test
    @DisplayName("✅ Lấy đường dẫn tệp tin DICOM gốc thành công")
    void testGetDicomViewerFilePath_Success() {
        // Giả lập câu lệnh SQL đơn truy vấn trường file_path
        when(jdbcTemplate.queryForObject(anyString(), eq(String.class), eq("chest_xray.dcm")))
                .thenReturn("dicom_uploads/chest_xray.dcm");

        String filePath = dicomViewerService.getDicomViewerFilePath("chest_xray.dcm");

        assertEquals("dicom_uploads/chest_xray.dcm", filePath);
        verify(jdbcTemplate, times(1)).queryForObject(anyString(), eq(String.class), eq("chest_xray.dcm"));
    }
}