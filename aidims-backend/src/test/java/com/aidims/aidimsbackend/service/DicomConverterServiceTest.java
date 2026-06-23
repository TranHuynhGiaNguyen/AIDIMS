package com.aidims.aidimsbackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DicomConverterService - Unit Tests")
class DicomConverterServiceTest {

    @InjectMocks
    private DicomConverterService dicomConverterService;

    private byte[] invalidDicomBytes;
    private byte[] minimalDicomHeader;

    @BeforeEach
    void setUp() {
        invalidDicomBytes = new byte[]{1, 2, 3, 4, 5};
        
        minimalDicomHeader = new byte[132];
        minimalDicomHeader[128] = 'D';
        minimalDicomHeader[129] = 'I';
        minimalDicomHeader[130] = 'C';
        minimalDicomHeader[131] = 'M';
    }

    @Test
    @DisplayName("❌ convert - Ném ngoại lệ khi truyền mảng byte rỗng")
    void convert_EmptyBytes_ThrowsException() {
        byte[] emptyBytes = new byte[0];
        assertThrows(Exception.class, () -> dicomConverterService.convert(emptyBytes));
    }

    @Test
    @DisplayName("❌ convert - Ném ngoại lệ khi tệp DICOM bị lỗi định dạng hoặc bị cắt cụt")
    void convert_InvalidOrTruncatedDicom_ThrowsException() {
        // Tệp DICOM có header tối thiểu nhưng không có pixel data
        assertThrows(Exception.class, () -> dicomConverterService.convert(minimalDicomHeader));
    }

    @Test
    @DisplayName("❌ convert - Ném ngoại lệ khi tệp tin không có định dạng DICOM hợp lệ")
    void convert_NotADicomFile_ThrowsException() {
        assertThrows(Exception.class, () -> dicomConverterService.convert(invalidDicomBytes));
    }

    @Test
    @DisplayName("❌ Lỗi nghiệp vụ: Ném ngoại lệ nghiệp vụ cụ thể khi truyền mảng byte null thay vì NullPointerException")
    void convert_NullBytes_ThrowsIllegalArgumentException() {
        // Thực tế code sẽ ném ra NullPointerException do ByteArrayInputStream không nhận null.
        // Test mong đợi ném ra IllegalArgumentException với thông điệp rõ ràng, dẫn đến test này FAIL.
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
            () -> dicomConverterService.convert(null),
            "Ném IllegalArgumentException khi đầu vào null"
        );
        assertTrue(ex.getMessage().contains("null"));
    }
}
