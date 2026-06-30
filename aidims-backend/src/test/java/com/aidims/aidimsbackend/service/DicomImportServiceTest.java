package com.aidims.aidimsbackend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aidims.aidimsbackend.entity.DicomImport;
import com.aidims.aidimsbackend.repository.DicomImportRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("DicomImportService - Unit Tests")
class DicomImportServiceTest {

    @Mock
    private DicomImportRepository dicomImportRepository;

    @InjectMocks
    private DicomImportService dicomImportService;

    @Test
    @DisplayName("Khong duoc luu neu patientCode rong")
    void saveDicomImport_WithEmptyPatientCode_ShouldThrowException() {
        DicomImport dicom = new DicomImport();
        dicom.setPatientCode("");

        assertThrows(
                IllegalArgumentException.class,
                () -> dicomImportService.saveDicomImport(dicom));

        verify(dicomImportRepository, never()).save(any());
    }

    @Test
    @DisplayName("Khong duoc luu neu ten file khong hop le")
    void saveDicomImport_WithInvalidFileName_ShouldThrowException() {
        DicomImport dicom = new DicomImport();
        dicom.setPatientCode("BN001");
        dicom.setFileName("virus.exe");

        assertThrows(
                IllegalArgumentException.class,
                () -> dicomImportService.saveDicomImport(dicom));

        verify(dicomImportRepository, never()).save(any());
    }

    @Test
    @DisplayName("Luu thanh cong voi du lieu hop le")
    void saveDicomImport_WithValidData_ShouldSaveSuccessfully() {
        DicomImport dicom = new DicomImport();
        dicom.setPatientCode("BN001");
        dicom.setFileName("x_quang_phoi.dcm");

        when(dicomImportRepository.save(any(DicomImport.class)))
                .thenReturn(dicom);

        DicomImport result = dicomImportService.saveDicomImport(dicom);

        assertNotNull(result);
        assertEquals("BN001", result.getPatientCode());

        verify(dicomImportRepository, times(1)).save(dicom);
    }

    @Test
    @DisplayName("saveDicomImport tu dong gan importDate")
    void saveDicomImport_ShouldSetImportDateAndSaveEntity() {

        DicomImport dicomImport = new DicomImport();
        dicomImport.setPatientCode("BN001");
        dicomImport.setFileName("scan.dcm");
        dicomImport.setStatus("imported");

        when(dicomImportRepository.save(any(DicomImport.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DicomImport result =
                dicomImportService.saveDicomImport(dicomImport);

        ArgumentCaptor<DicomImport> captor =
                ArgumentCaptor.forClass(DicomImport.class);

        verify(dicomImportRepository).save(captor.capture());

        DicomImport saved = captor.getValue();

        assertNotNull(saved.getImportDate());
        assertNotNull(result.getImportDate());
        assertEquals(saved.getImportDate(), result.getImportDate());
    }
}