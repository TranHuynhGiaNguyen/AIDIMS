package com.aidims.aidimsbackend.controller;

import com.aidims.aidimsbackend.entity.DiagnosticReport;
import com.aidims.aidimsbackend.service.DiagnosticReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DiagnosticReportControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DiagnosticReportService diagnosticReportService;

    @InjectMocks
    private DiagnosticReportController diagnosticReportController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(diagnosticReportController).build();
    }

    @Test
    void testEndpoint_returnsSuccess() throws Exception {
        mockMvc.perform(get("/api/diagnostic-reports/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("API is working!"));
    }

    @Test
    void getReportById_whenReportExists_returnsReport() throws Exception {
        DiagnosticReport report = new DiagnosticReport();
        report.setReportId(1);
        report.setReportCode("BC20260616001");
        report.setFindings("Findings detail");
        report.setImpression("Impression detail");
        report.setReportType(DiagnosticReport.ReportType.ChinhThuc);
        report.setStatus(DiagnosticReport.ReportStatus.HoanThanh);

        Mockito.when(diagnosticReportService.getReportById(1)).thenReturn(Optional.of(report));

        mockMvc.perform(get("/api/diagnostic-reports/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Lấy báo cáo thành công"))
                .andExpect(jsonPath("$.data.reportId").value(1))
                .andExpect(jsonPath("$.data.reportCode").value("BC20260616001"));
    }

    @Test
    void getReportById_whenReportDoesNotExist_returnsNotFound() throws Exception {
        Mockito.when(diagnosticReportService.getReportById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/diagnostic-reports/{id}", 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Không tìm thấy báo cáo với ID: 999"));
    }

    @Test
    void createReport_withValidData_returnsCreated() throws Exception {
        DiagnosticReport inputReport = new DiagnosticReport();
        inputReport.setResultId(1);
        inputReport.setReportCode("BC20260616001");
        inputReport.setFindings("Normal findings");
        inputReport.setImpression("Normal impression");
        inputReport.setReportType(DiagnosticReport.ReportType.ChinhThuc);
        inputReport.setRadiologistId(4);
        inputReport.setStatus(DiagnosticReport.ReportStatus.HoanThanh);

        DiagnosticReport savedReport = new DiagnosticReport();
        savedReport.setReportId(123);
        savedReport.setResultId(1);
        savedReport.setReportCode("BC20260616001");
        savedReport.setFindings("Normal findings");
        savedReport.setImpression("Normal impression");
        savedReport.setReportType(DiagnosticReport.ReportType.ChinhThuc);
        savedReport.setRadiologistId(4);
        savedReport.setStatus(DiagnosticReport.ReportStatus.HoanThanh);

        Mockito.when(diagnosticReportService.createReport(any(DiagnosticReport.class))).thenReturn(savedReport);

        mockMvc.perform(post("/api/diagnostic-reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputReport)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Báo cáo chẩn đoán đã được tạo thành công"))
                .andExpect(jsonPath("$.data.reportId").value(123))
                .andExpect(jsonPath("$.data.reportCode").value("BC20260616001"));
    }

    @Test
    void createReport_withServiceException_returnsBadRequest() throws Exception {
        DiagnosticReport inputReport = new DiagnosticReport();
        inputReport.setResultId(1);
        inputReport.setReportCode("BC20260616001");
        inputReport.setFindings("Normal findings");
        inputReport.setImpression("Normal impression");

        Mockito.when(diagnosticReportService.createReport(any(DiagnosticReport.class)))
                .thenThrow(new RuntimeException("Report code already exists: BC20260616001"));

        mockMvc.perform(post("/api/diagnostic-reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputReport)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Lỗi khi tạo báo cáo: Report code already exists: BC20260616001"));
    }

    @Test
    void createReport_shouldReturnCorrectReportCodePrefix() throws Exception {
        DiagnosticReport inputReport = new DiagnosticReport();
        inputReport.setResultId(1);
        inputReport.setReportCode("BC20260616001");

        DiagnosticReport savedReport = new DiagnosticReport();
        savedReport.setReportId(123);
        savedReport.setReportCode("BC20260616001");

        Mockito.when(diagnosticReportService.createReport(any(DiagnosticReport.class))).thenReturn(savedReport);

        // Kỳ vọng tiếp đầu ngữ trong trả về là "REPORT-20260616001" thay vì "BC20260616001".
        // Test case này sẽ FAIL do hệ thống trả về "BC20260616001".
        mockMvc.perform(post("/api/diagnostic-reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputReport)))
                .andExpect(jsonPath("$.data.reportCode").value("REPORT-20260616001"));
    }
}
