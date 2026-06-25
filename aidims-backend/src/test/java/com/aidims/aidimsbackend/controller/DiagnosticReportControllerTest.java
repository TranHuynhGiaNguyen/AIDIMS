package com.aidims.aidimsbackend.controller;

import com.aidims.aidimsbackend.entity.DiagnosticReport;
import com.aidims.aidimsbackend.service.DiagnosticReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DiagnosticReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class DiagnosticReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DiagnosticReportService diagnosticReportService;

    @Autowired
    private ObjectMapper objectMapper;

    private DiagnosticReport createSampleReport() {
        DiagnosticReport report = new DiagnosticReport();

        report.setReportId(1);
        report.setResultId(100);
        report.setReportCode("BC20260707001");
        report.setFindings("Normal findings");
        report.setImpression("Normal impression");
        report.setRecommendations("No recommendations");
        report.setReportType(DiagnosticReport.ReportType.SoBo);
        report.setStatus(DiagnosticReport.ReportStatus.BanNhap);
        report.setRadiologistId(1);
        report.setDictatedAt(LocalDateTime.now());

        report.setReferringDoctorName("Dr Test");
        report.setReferringDoctorSpecialty("Radiology");

        return report;
    }

    @Test
    void test_shouldReturnOk() throws Exception {

        mockMvc.perform(get("/api/diagnostic-reports/test"))
                .andExpect(status().isOk())
                .andExpect(content().string("API is working!"));
    }

    @Test
    void handleOptions_shouldReturnOk() throws Exception {

        mockMvc.perform(options("/api/diagnostic-reports"))
                .andExpect(status().isOk());
    }

    @Test
    void createReport_shouldReturnCreated() throws Exception {

        DiagnosticReport report = createSampleReport();

        when(diagnosticReportService.createReport(any(DiagnosticReport.class)))
                .thenReturn(report);

        mockMvc.perform(post("/api/diagnostic-reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(report)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reportCode")
                        .value("BC20260707001"));
    }

    @Test
    void createReport_whenException_shouldReturnBadRequest() throws Exception {

        DiagnosticReport report = createSampleReport();

        when(diagnosticReportService.createReport(any(DiagnosticReport.class)))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/api/diagnostic-reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(report)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void createReportAlternative_shouldReturnCreated() throws Exception {

        DiagnosticReport report = createSampleReport();

        when(diagnosticReportService.createReport(any(DiagnosticReport.class)))
                .thenReturn(report);

        mockMvc.perform(post("/api/diagnostic-reports/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(report)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void debugPost_shouldReturnOk() throws Exception {

        mockMvc.perform(post("/api/diagnostic-reports/test-post"))
                .andExpect(status().isOk())
                .andExpect(content().string("POST endpoint is working!"));
    }

    @Test
    void getAllReports_shouldReturnOk() throws Exception {

        DiagnosticReport report = createSampleReport();

        when(diagnosticReportService.getAllReports())
                .thenReturn(List.of(report));

        mockMvc.perform(get("/api/diagnostic-reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].reportCode")
                        .value("BC20260707001"));
    }

    @Test
    void getAllReports_whenException_shouldReturnInternalServerError()
            throws Exception {

        when(diagnosticReportService.getAllReports())
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/diagnostic-reports"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getReportById_whenFound_shouldReturnOk() throws Exception {

        DiagnosticReport report = createSampleReport();

        when(diagnosticReportService.getReportById(1))
                .thenReturn(Optional.of(report));

        mockMvc.perform(get("/api/diagnostic-reports/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reportCode")
                        .value("BC20260707001"));
    }

    @Test
    void getReportById_whenNotFound_shouldReturnNotFound()
            throws Exception {

        when(diagnosticReportService.getReportById(1))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/diagnostic-reports/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getReportById_whenException_shouldReturnInternalServerError()
            throws Exception {

        when(diagnosticReportService.getReportById(1))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/diagnostic-reports/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getReportStatistics_shouldReturnOk() throws Exception {

        DiagnosticReportService.ReportStatistics stats =
                new DiagnosticReportService.ReportStatistics(
                        10,
                        4,
                        6
                );

        when(diagnosticReportService.getReportStatistics())
                .thenReturn(stats);

        mockMvc.perform(get("/api/diagnostic-reports/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalReports").value(10))
                .andExpect(jsonPath("$.data.draftReports").value(4))
                .andExpect(jsonPath("$.data.completedReports").value(6));
    }

    @Test
    void getReportStatistics_whenException_shouldReturnInternalServerError()
            throws Exception {

        when(diagnosticReportService.getReportStatistics())
                .thenThrow(new RuntimeException("Statistics error"));

        mockMvc.perform(get("/api/diagnostic-reports/statistics"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void generateReportCode_shouldReturnOk() throws Exception {

        when(diagnosticReportService.generateReportCode())
                .thenReturn("BC20260707001");

        mockMvc.perform(get("/api/diagnostic-reports/generate-code"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data")
                        .value("BC20260707001"));
    }

    @Test
    void generateReportCode_whenException_shouldReturnInternalServerError()
            throws Exception {

        when(diagnosticReportService.generateReportCode())
                .thenThrow(new RuntimeException("Code generation error"));

        mockMvc.perform(get("/api/diagnostic-reports/generate-code"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void generateReportCodePost_shouldReturnOk() throws Exception {

        when(diagnosticReportService.generateReportCode())
                .thenReturn("BC20260707001");

        mockMvc.perform(post("/api/diagnostic-reports/generate-code"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data")
                        .value("BC20260707001"));
    }

    @Test
    void generateReportCodePost_whenException_shouldReturnInternalServerError()
            throws Exception {

        when(diagnosticReportService.generateReportCode())
                .thenThrow(new RuntimeException("Code generation error"));

        mockMvc.perform(post("/api/diagnostic-reports/generate-code"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }
}