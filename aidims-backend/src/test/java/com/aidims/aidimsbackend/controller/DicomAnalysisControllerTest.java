package com.aidims.aidimsbackend.controller;

import com.aidims.aidimsbackend.dto.DicomAnalysisResponse;
import com.aidims.aidimsbackend.service.ChatService;
import com.aidims.aidimsbackend.service.DicomConverterService;
import com.aidims.aidimsbackend.service.DicomConverterService.ConvertResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DicomAnalysisControllerTest - File Extensions")
class DicomAnalysisControllerTest {

    @Mock
    private DicomConverterService dicomConverter;

    @Mock
    private ChatService chatService;

    @InjectMocks
    private DicomAnalysisController dicomAnalysisController;

    private byte[] fakeDicomContent;
    private ConvertResult mockConvertResult;
    private String expectedAnalysisText;

    @BeforeEach
    void setUp() {
        fakeDicomContent = new byte[132];
        fakeDicomContent[128] = 'D';
        fakeDicomContent[129] = 'I';
        fakeDicomContent[130] = 'C';
        fakeDicomContent[131] = 'M';

        DicomAnalysisResponse.DicomMetadata metadata = new DicomAnalysisResponse.DicomMetadata();
        metadata.setModality("CT");
        metadata.setBodyPart("CHEST");
        mockConvertResult = new ConvertResult("mockBase64", metadata);
        expectedAnalysisText = "Normal chest X-ray. No abnormal findings.";
    }

    @Test
    @DisplayName("✅ File .dcm được chấp nhận")
    void testDcmExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "scan.dcm", "application/octet-stream", fakeDicomContent
        );
        when(dicomConverter.convert(any(byte[].class))).thenReturn(mockConvertResult);
        when(chatService.analyzeImages(any())).thenReturn(expectedAnalysisText);

        ResponseEntity<DicomAnalysisResponse> response =
                dicomAnalysisController.analyzeDicom(file, "Test", null, null);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("success", response.getBody().getStatus());
        assertEquals(expectedAnalysisText, response.getBody().getAnalysisText());
        verify(dicomConverter, times(1)).convert(any(byte[].class));
        verify(chatService, times(1)).analyzeImages(any());
    }

    @Test
    @DisplayName("✅ File .dicom được chấp nhận")
    void testDicomExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "scan.dicom", "application/octet-stream", fakeDicomContent
        );
        when(dicomConverter.convert(any(byte[].class))).thenReturn(mockConvertResult);
        when(chatService.analyzeImages(any())).thenReturn(expectedAnalysisText);

        ResponseEntity<DicomAnalysisResponse> response =
                dicomAnalysisController.analyzeDicom(file, "Test", null, null);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("success", response.getBody().getStatus());
        verify(dicomConverter, times(1)).convert(any(byte[].class));
        verify(chatService, times(1)).analyzeImages(any());
    }

    @Test
    @DisplayName("✅ File .dc3 được chấp nhận")
    void testDc3Extension() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "scan.dc3", "application/octet-stream", fakeDicomContent
        );
        when(dicomConverter.convert(any(byte[].class))).thenReturn(mockConvertResult);
        when(chatService.analyzeImages(any())).thenReturn(expectedAnalysisText);

        ResponseEntity<DicomAnalysisResponse> response =
                dicomAnalysisController.analyzeDicom(file, "Test", null, null);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("success", response.getBody().getStatus());
        verify(dicomConverter, times(1)).convert(any(byte[].class));
        verify(chatService, times(1)).analyzeImages(any());
    }

    @Test
    @DisplayName("✅ File .dic được chấp nhận")
    void testDicExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "scan.dic", "application/octet-stream", fakeDicomContent
        );
        when(dicomConverter.convert(any(byte[].class))).thenReturn(mockConvertResult);
        when(chatService.analyzeImages(any())).thenReturn(expectedAnalysisText);

        ResponseEntity<DicomAnalysisResponse> response =
                dicomAnalysisController.analyzeDicom(file, "Test", null, null);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("success", response.getBody().getStatus());
        verify(dicomConverter, times(1)).convert(any(byte[].class));
        verify(chatService, times(1)).analyzeImages(any());
    }

    @Test
    @DisplayName("✅ File không có đuôi (từ PACS) được chấp nhận")
    void testBlankExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "IM000001", "application/octet-stream", fakeDicomContent
        );
        when(dicomConverter.convert(any(byte[].class))).thenReturn(mockConvertResult);
        when(chatService.analyzeImages(any())).thenReturn(expectedAnalysisText);

        ResponseEntity<DicomAnalysisResponse> response =
                dicomAnalysisController.analyzeDicom(file, "Test", null, null);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("success", response.getBody().getStatus());
        verify(dicomConverter, times(1)).convert(any(byte[].class));
        verify(chatService, times(1)).analyzeImages(any());
    }

    @Test
    @DisplayName("✅ File .DCM (chữ hoa) được chấp nhận")
    void testUpperCaseExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "SCAN.DCM", "application/octet-stream", fakeDicomContent
        );
        when(dicomConverter.convert(any(byte[].class))).thenReturn(mockConvertResult);
        when(chatService.analyzeImages(any())).thenReturn(expectedAnalysisText);

        ResponseEntity<DicomAnalysisResponse> response =
                dicomAnalysisController.analyzeDicom(file, "Test", null, null);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("success", response.getBody().getStatus());
        verify(dicomConverter, times(1)).convert(any(byte[].class));
        verify(chatService, times(1)).analyzeImages(any());
    }

    @Test
    @DisplayName("❌ File .txt bị từ chối, không gọi service")
    void testTxtExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "document.txt", "text/plain", "content".getBytes()
        );

        ResponseEntity<DicomAnalysisResponse> response =
                dicomAnalysisController.analyzeDicom(file, "Test", null, null);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("error", response.getBody().getStatus());
        verify(dicomConverter, never()).convert(any(byte[].class));
        verify(chatService, never()).analyzeImages(any());
    }

    @Test
    @DisplayName("❌ File .jpg bị từ chối, không gọi service")
    void testJpgExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "image.jpg", "image/jpeg", "image".getBytes()
        );

        ResponseEntity<DicomAnalysisResponse> response =
                dicomAnalysisController.analyzeDicom(file, "Test", null, null);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("error", response.getBody().getStatus());
        verify(dicomConverter, never()).convert(any(byte[].class));
        verify(chatService, never()).analyzeImages(any());
    }

    @Test
    @DisplayName("❌ File .dcm.exe (giả mạo) bị từ chối, không gọi service")
    void testFakeDicomExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "virus.dcm.exe", "application/octet-stream", "exe".getBytes()
        );

        ResponseEntity<DicomAnalysisResponse> response =
                dicomAnalysisController.analyzeDicom(file, "Test", null, null);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("error", response.getBody().getStatus());
        verify(dicomConverter, never()).convert(any(byte[].class));
        verify(chatService, never()).analyzeImages(any());
    }

    @Test
    @DisplayName("❌ File rỗng bị từ chối, không gọi service")
    void testEmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.dcm", "application/octet-stream", new byte[0]
        );

        ResponseEntity<DicomAnalysisResponse> response =
                dicomAnalysisController.analyzeDicom(file, "Test", null, null);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("error", response.getBody().getStatus());
        assertEquals("File rỗng", response.getBody().getAnalysisText());
        verify(dicomConverter, never()).convert(any(byte[].class));
        verify(chatService, never()).analyzeImages(any());
    }

    @Test
    @DisplayName("❌ File quá lớn (>100MB) bị từ chối, không gọi service")
    void testFileTooLarge() throws Exception {
        MockMultipartFile file = Mockito.mock(MockMultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(101L * 1024 * 1024);
        when(file.getOriginalFilename()).thenReturn("large.dcm");

        ResponseEntity<DicomAnalysisResponse> response =
                dicomAnalysisController.analyzeDicom(file, "Test", null, null);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("error", response.getBody().getStatus());
        assertEquals("File quá lớn (tối đa 100MB)", response.getBody().getAnalysisText());
        verify(dicomConverter, never()).convert(any(byte[].class));
        verify(chatService, never()).analyzeImages(any());
    }

    @Test
    @DisplayName("⚠️ DicomConverterService ném exception → controller fallback trả về 200")
    void testConvertException_fallback() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "scan.dcm", "application/octet-stream", fakeDicomContent
        );

        when(dicomConverter.convert(any(byte[].class)))
                .thenThrow(new RuntimeException("Convert failed"));

        ResponseEntity<DicomAnalysisResponse> response =
                dicomAnalysisController.analyzeDicom(file, "Test", null, null);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("success", response.getBody().getStatus());
        assertNotNull(response.getBody().getAnalysisText());
        assertNotNull(response.getBody().getDicomImageBase64());
        verify(dicomConverter, times(1)).convert(any(byte[].class));
        verify(chatService, never()).analyzeImages(any());
    }

    @Test
    @DisplayName("⚠️ ChatService ném exception → controller fallback")
    void testChatServiceException_fallback() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "scan.dcm", "application/octet-stream", fakeDicomContent
        );

        when(dicomConverter.convert(any(byte[].class))).thenReturn(mockConvertResult);
        when(chatService.analyzeImages(any()))
                .thenThrow(new RuntimeException("Chat service failed"));

        ResponseEntity<DicomAnalysisResponse> response =
                dicomAnalysisController.analyzeDicom(file, "Test", null, null);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("success", response.getBody().getStatus());
        verify(dicomConverter, times(1)).convert(any(byte[].class));
        verify(chatService, times(1)).analyzeImages(any());
    }
}