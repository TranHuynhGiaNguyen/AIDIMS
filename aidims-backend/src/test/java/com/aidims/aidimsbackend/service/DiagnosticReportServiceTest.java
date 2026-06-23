package com.aidims.aidimsbackend.service;

import com.aidims.aidimsbackend.entity.DiagnosticReport;
import com.aidims.aidimsbackend.repository.DiagnosticReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DiagnosticReportService - Unit Tests")
class DiagnosticReportServiceTest {

    @Mock
    private DiagnosticReportRepository reportRepo;

    @InjectMocks
    private DiagnosticReportService reportService;

    private DiagnosticReport sampleReport;

    @BeforeEach
    void setUp() {
        sampleReport = new DiagnosticReport();
        sampleReport.setReportId(1);
        sampleReport.setResultId(10);
        sampleReport.setReportCode("BC20260624001");
        sampleReport.setFindings("Sample findings");
        sampleReport.setImpression("Sample impression");
        sampleReport.setReportType(DiagnosticReport.ReportType.ChinhThuc);
        sampleReport.setRadiologistId(5);
        sampleReport.setStatus(DiagnosticReport.ReportStatus.BanNhap);
        sampleReport.setDictatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("✅ generateReportCode - Sinh mã báo cáo tự động khi đếm trong DB thành công")
    void generateReportCode_Success() {
        when(reportRepo.count()).thenReturn(5L);
        String code = reportService.generateReportCode();
        assertNotNull(code);
        assertTrue(code.startsWith("BC"));
        assertTrue(code.endsWith("006")); // count + 1 = 6
    }

    @Test
    @DisplayName("✅ generateReportCode - Fallback sinh mã kết thúc bằng 001 khi DB đếm bị lỗi")
    void generateReportCode_FallbackOnException() {
        when(reportRepo.count()).thenThrow(new RuntimeException("Database error"));
        String code = reportService.generateReportCode();
        assertNotNull(code);
        assertTrue(code.startsWith("BC"));
        assertTrue(code.endsWith("001"));
    }

    @Test
    @DisplayName("✅ createReport - Tạo báo cáo thành công với đầy đủ dữ liệu")
    void createReport_Success() {
        when(reportRepo.existsByReportCode("BC20260624001")).thenReturn(false);
        when(reportRepo.save(any(DiagnosticReport.class))).thenAnswer(inv -> inv.getArgument(0));

        sampleReport.setReferringDoctorName("  Dr. John Doe  ");
        sampleReport.setReferringDoctorSpecialty("  Cardiology  ");

        DiagnosticReport result = reportService.createReport(sampleReport);

        assertNotNull(result);
        assertEquals("BC20260624001", result.getReportCode());
        assertEquals("Dr. John Doe", result.getReferringDoctorName());
        assertEquals("Cardiology", result.getReferringDoctorSpecialty());
        verify(reportRepo, times(1)).save(sampleReport);
    }

    @Test
    @DisplayName("✅ createReport - Tự động sinh mã báo cáo nếu truyền vào null hoặc rỗng")
    void createReport_GenerateCodeIfNullOrEmpty() {
        sampleReport.setReportCode(null);
        when(reportRepo.count()).thenReturn(10L);
        when(reportRepo.existsByReportCode(anyString())).thenReturn(false);
        when(reportRepo.save(any(DiagnosticReport.class))).thenAnswer(inv -> inv.getArgument(0));

        DiagnosticReport result = reportService.createReport(sampleReport);

        assertNotNull(result.getReportCode());
        assertTrue(result.getReportCode().startsWith("BC"));
    }

    @Test
    @DisplayName("❌ createReport - Ném ngoại lệ khi mã báo cáo đã tồn tại trong DB")
    void createReport_ThrowsExceptionWhenCodeExists() {
        when(reportRepo.existsByReportCode("BC20260624001")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> reportService.createReport(sampleReport));
        assertTrue(ex.getMessage().contains("Report code already exists"));
        verify(reportRepo, never()).save(any(DiagnosticReport.class));
    }

    @Test
    @DisplayName("✅ updateReport - Cập nhật thành công các trường thông tin")
    void updateReport_Success() {
        when(reportRepo.findById(1)).thenReturn(Optional.of(sampleReport));
        when(reportRepo.save(any(DiagnosticReport.class))).thenAnswer(inv -> inv.getArgument(0));

        DiagnosticReport updateData = new DiagnosticReport();
        updateData.setFindings("Updated findings");
        updateData.setImpression("Updated impression");
        updateData.setRecommendations("New recommendation");
        updateData.setReportType(DiagnosticReport.ReportType.CapCuu);
        updateData.setStatus(DiagnosticReport.ReportStatus.BanNhap);

        DiagnosticReport result = reportService.updateReport(1, updateData);

        assertNotNull(result);
        assertEquals("Updated findings", result.getFindings());
        assertEquals("Updated impression", result.getImpression());
        assertEquals("New recommendation", result.getRecommendations());
        assertEquals(DiagnosticReport.ReportType.CapCuu, result.getReportType());
    }

    @Test
    @DisplayName("✅ updateReport - Tự động gán thời gian hoàn thành khi status đổi thành HoanThanh")
    void updateReport_SetsFinalizedAtWhenCompleted() {
        when(reportRepo.findById(1)).thenReturn(Optional.of(sampleReport));
        when(reportRepo.save(any(DiagnosticReport.class))).thenAnswer(inv -> inv.getArgument(0));

        DiagnosticReport updateData = new DiagnosticReport();
        updateData.setStatus(DiagnosticReport.ReportStatus.HoanThanh);

        assertNull(sampleReport.getFinalizedAt());
        DiagnosticReport result = reportService.updateReport(1, updateData);

        assertEquals(DiagnosticReport.ReportStatus.HoanThanh, result.getStatus());
        assertNotNull(result.getFinalizedAt());
    }

    @Test
    @DisplayName("❌ updateReport - Ném ngoại lệ khi không tìm thấy báo cáo theo ID")
    void updateReport_NotFound() {
        when(reportRepo.findById(999)).thenReturn(Optional.empty());

        DiagnosticReport updateData = new DiagnosticReport();

        RuntimeException ex = assertThrows(RuntimeException.class, () -> reportService.updateReport(999, updateData));
        assertTrue(ex.getMessage().contains("Diagnostic report not found"));
    }

    @Test
    @DisplayName("✅ finalizeReport - Hoàn thành báo cáo và gán thời gian hoàn thành")
    void finalizeReport_Success() {
        when(reportRepo.findById(1)).thenReturn(Optional.of(sampleReport));
        when(reportRepo.save(any(DiagnosticReport.class))).thenAnswer(inv -> inv.getArgument(0));

        DiagnosticReport result = reportService.finalizeReport(1);

        assertEquals(DiagnosticReport.ReportStatus.HoanThanh, result.getStatus());
        assertNotNull(result.getFinalizedAt());
    }

    @Test
    @DisplayName("✅ getReportStatistics - Tính toán chính xác số liệu thống kê")
    void getReportStatistics_Success() {
        when(reportRepo.count()).thenReturn(20L);
        when(reportRepo.countByStatus(DiagnosticReport.ReportStatus.BanNhap)).thenReturn(5L);
        when(reportRepo.countByStatus(DiagnosticReport.ReportStatus.HoanThanh)).thenReturn(15L);

        DiagnosticReportService.ReportStatistics stats = reportService.getReportStatistics();

        assertEquals(20L, stats.getTotalReports());
        assertEquals(5L, stats.getDraftReports());
        assertEquals(15L, stats.getCompletedReports());
    }

    @Test
    @DisplayName("✅ deleteReport - Xóa báo cáo thành công")
    void deleteReport_Success() {
        when(reportRepo.existsById(1)).thenReturn(true);
        doNothing().when(reportRepo).deleteById(1);

        assertDoesNotThrow(() -> reportService.deleteReport(1));
        verify(reportRepo, times(1)).deleteById(1);
    }

    @Test
    @DisplayName("❌ deleteReport - Ném ngoại lệ khi báo cáo cần xóa không tồn tại")
    void deleteReport_NotFound() {
        when(reportRepo.existsById(999)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> reportService.deleteReport(999));
        assertTrue(ex.getMessage().contains("Diagnostic report not found"));
        verify(reportRepo, never()).deleteById(anyInt());
    }

    @Test
    @DisplayName("✅ getAllReports - Lấy danh sách toàn bộ báo cáo")
    void getAllReports_Success() {
        List<DiagnosticReport> mockList = Arrays.asList(sampleReport);
        when(reportRepo.findAll()).thenReturn(mockList);

        List<DiagnosticReport> result = reportService.getAllReports();
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("✅ getReportsByRadiologist - Lấy danh sách báo cáo theo ID bác sĩ đọc phim")
    void getReportsByRadiologist_Success() {
        List<DiagnosticReport> mockList = Arrays.asList(sampleReport);
        when(reportRepo.findByRadiologistId(5)).thenReturn(mockList);

        List<DiagnosticReport> result = reportService.getReportsByRadiologist(5);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("❌ Lỗi nghiệp vụ: Không được phép cập nhật lại thời gian hoàn thành (finalizedAt) của báo cáo đã HoanThanh trước đó")
    void finalizeReport_AlreadyFinalized_ShouldNotOverrideFinalizedAt() {
        LocalDateTime originalFinalized = LocalDateTime.now().minusDays(10);
        sampleReport.setStatus(DiagnosticReport.ReportStatus.HoanThanh);
        sampleReport.setFinalizedAt(originalFinalized);

        when(reportRepo.findById(1)).thenReturn(Optional.of(sampleReport));
        when(reportRepo.save(any(DiagnosticReport.class))).thenAnswer(inv -> inv.getArgument(0));

        DiagnosticReport result = reportService.finalizeReport(1);

        // Thời gian finalizedAt không được đổi
        assertEquals(originalFinalized, result.getFinalizedAt(), 
            "Không được ghi đè finalizedAt của báo cáo đã hoàn thành từ trước");
    }

    @Test
    @DisplayName("❌ Lỗi nghiệp vụ: Không được phép cập nhật thông tin chẩn đoán (findings, impression) khi báo cáo đã ở trạng thái HoanThanh")
    void updateReport_WhenAlreadyCompleted_ShouldThrowException() {
        sampleReport.setStatus(DiagnosticReport.ReportStatus.HoanThanh);

        when(reportRepo.findById(1)).thenReturn(Optional.of(sampleReport));

        DiagnosticReport updateData = new DiagnosticReport();
        updateData.setFindings("New changes");

        // Mong đợi ném ra RuntimeException vì báo cáo đã hoàn thành không được phép sửa
        // Thực tế code không kiểm tra và vẫn cho phép sửa, dẫn đến test này bị FAIL
        assertThrows(IllegalStateException.class, () -> reportService.updateReport(1, updateData),
            "Sửa báo cáo đã hoàn thành phải ném ra ngoại lệ");
    }
}
