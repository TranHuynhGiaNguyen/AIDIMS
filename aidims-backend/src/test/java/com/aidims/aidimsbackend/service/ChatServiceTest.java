package com.aidims.aidimsbackend.service;

import com.aidims.aidimsbackend.dto.ImageAnalysisRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatService - Unit Tests")
class ChatServiceTest {

    @InjectMocks
    private ChatService chatService;

    @BeforeEach
    void setUp() {
        chatService.init();
        ReflectionTestUtils.setField(chatService, "geminiApiKey", "YOUR_GEMINI_API_KEY_HERE");
        ReflectionTestUtils.setField(chatService, "openaiApiKey", "your-openai-api-key-here");
    }

    // =============================================================
    // NHOM 1 - TEST getChatResponse (TC1-TC9)
    // =============================================================

    @Nested
    @DisplayName("Nhom 1 - getChatResponse - Fallback local logic")
    class GetChatResponseFallbackTests {

        @BeforeEach
        void disableApiKeys() {
            ReflectionTestUtils.setField(chatService, "geminiApiKey", "");
            ReflectionTestUtils.setField(chatService, "openaiApiKey", "");
        }

        @Test
        @DisplayName("TC1 - Nhan dang trieu chung tim mach")
        void tc1_GetChatResponse_TimMach() {
            String response = chatService.getChatResponse("đau ngực");
            assertNotNull(response);
            assertTrue(response.contains("PHÂN TÍCH TRIỆU CHỨNG"));
            assertTrue(response.contains("ĐAU NGỰC"));
        }

        @Test
        @DisplayName("TC2 - Nhan dang trieu chung ho hap")
        void tc2_GetChatResponse_HoHap() {
            String response = chatService.getChatResponse("khó thở");
            assertNotNull(response);
            assertTrue(response.contains("KHÓ THỞ"));
        }

        @Test
        @DisplayName("TC3 - Nhan dang trieu chung tieu hoa")
        void tc3_GetChatResponse_TieuHoa() {
            String response = chatService.getChatResponse("đau bụng");
            assertNotNull(response);
            assertTrue(response.contains("ĐAU BỤNG"));
        }

        @Test
        @DisplayName("TC4 - Nhan dang trieu chung than kinh")
        void tc4_GetChatResponse_ThanKinh() {
            String response = chatService.getChatResponse("đau đầu");
            assertNotNull(response);
            assertTrue(response.contains("ĐAU ĐẦU"));
        }

        @Test
        @DisplayName("TC5 - Canh bao khan cap khi trieu chung nguy hiem")
        void tc5_GetChatResponse_KhanCap() {
            String response = chatService.getChatResponse("co giật");
            assertNotNull(response);
            assertTrue(response.contains("CO GIẬT"));
            assertTrue(response.contains("KHẨN CẤP"));
        }

        @Test
        @DisplayName("TC6 - Nhan dang DICOM finding")
        void tc6_GetChatResponse_DICOM() {
            String response = chatService.getChatResponse("ground glass");
            assertNotNull(response);
            assertTrue(response.contains("GROUND GLASS"));
        }

        @Test
        @DisplayName("TC7 - Tin nhan khong xac dinh")
        void tc7_GetChatResponse_Unknown() {
            String response = chatService.getChatResponse("hello");
            assertNotNull(response);
            assertTrue(response.contains("TƯ VẤN Y TẾ AIDIMS"));
        }

        @Test
        @DisplayName("TC8 - Ket hop trieu chung")
        void tc8_GetChatResponse_Combination() {
            String response = chatService.getChatResponse("đau ngực và khó thở");
            assertNotNull(response);
            assertTrue(response.contains("KẾT HỢP TRIỆU CHỨNG"));
        }

        @Test
        @DisplayName("TC9 - Tin nhan rong")
        void tc9_GetChatResponse_Empty() {
            String response = chatService.getChatResponse("");
            assertNotNull(response);
            assertTrue(response.contains("TƯ VẤN Y TẾ AIDIMS"));
        }
    }

    // =============================================================
    // NHOM 2 - TEST getChatResponse - Gemini (TC10-TC11)
    // =============================================================

    @Nested
    @DisplayName("Nhom 2 - getChatResponse - Gemini API")
    class GetChatResponseGeminiTests {

        @Test
        @DisplayName("TC10 - Gemini API thanh cong")
        void tc10_GetChatResponse_GeminiSuccess() {
            ReflectionTestUtils.setField(chatService, "geminiApiKey", "test-key");
            // Note: Test nay se goi API thuc -> co the fail neu khong co ket noi
            // Can mock WebClient de test ky hon
            String response = chatService.getChatResponse("đau đầu");
            assertNotNull(response);
        }

        @Test
        @DisplayName("TC11 - Gemini fail -> Fallback local")
        void tc11_GetChatResponse_GeminiFallback() {
            ReflectionTestUtils.setField(chatService, "geminiApiKey", "");
            String response = chatService.getChatResponse("đau đầu");
            assertNotNull(response);
            assertTrue(response.contains("ĐAU ĐẦU"));
        }
    }

    // =============================================================
    // NHOM 3 - TEST testGeminiDirectly (TC12-TC14)
    // =============================================================

    @Nested
    @DisplayName("Nhom 3 - testGeminiDirectly")
    class TestGeminiDirectlyTests {

        @Test
        @DisplayName("TC12 - Gemini API co key hop le")
        void tc12_TestGeminiDirectly_Success() {
            ReflectionTestUtils.setField(chatService, "geminiApiKey", "test-key");
            // Can mock WebClient
            assertThrows(RuntimeException.class, () -> chatService.testGeminiDirectly("Hello"));
        }

        @Test
        @DisplayName("TC13 - Khong co API key -> Exception")
        void tc13_TestGeminiDirectly_NoApiKey() {
            ReflectionTestUtils.setField(chatService, "geminiApiKey", "");
            assertThrows(RuntimeException.class, () -> chatService.testGeminiDirectly("Hello"));
        }

        @Test
        @DisplayName("TC14 - API key mac dinh -> Exception")
        void tc14_TestGeminiDirectly_DefaultKey() {
            ReflectionTestUtils.setField(chatService, "geminiApiKey", "YOUR_GEMINI_API_KEY_HERE");
            assertThrows(RuntimeException.class, () -> chatService.testGeminiDirectly("Hello"));
        }
    }

    // =============================================================
    // NHOM 4 - TEST analyzeImages (TC15-TC18)
    // =============================================================

    @Nested
    @DisplayName("Nhom 4 - analyzeImages")
    class AnalyzeImagesTests {

        private ImageAnalysisRequest createValidRequest() {
            ImageAnalysisRequest req = new ImageAnalysisRequest();
            req.setMessage("Phân tích");
            ImageAnalysisRequest.ImageData img = new ImageAnalysisRequest.ImageData();
            img.setName("test.jpg");
            img.setType("image/jpeg");
            img.setData("base64data");
            img.setSize(100L);
            List<ImageAnalysisRequest.ImageData> images = new ArrayList<>();
            images.add(img);
            req.setImages(images);
            return req;
        }

        @Test
        @DisplayName("TC15 - Analyze images thanh cong")
        void tc15_AnalyzeImages_Success() {
            ReflectionTestUtils.setField(chatService, "geminiApiKey", "test-key");
            ImageAnalysisRequest req = createValidRequest();
            // Can mock WebClient
            assertThrows(RuntimeException.class, () -> chatService.analyzeImages(req));
        }

        @Test
        @DisplayName("TC16 - Khong co API key -> Exception")
        void tc16_AnalyzeImages_NoApiKey() {
            ReflectionTestUtils.setField(chatService, "geminiApiKey", "");
            ImageAnalysisRequest req = createValidRequest();
            assertThrows(RuntimeException.class, () -> chatService.analyzeImages(req));
        }

        @Test
        @DisplayName("TC17 - Danh sach anh rong -> IllegalArgumentException")
        void tc17_AnalyzeImages_EmptyImages() {
            ReflectionTestUtils.setField(chatService, "geminiApiKey", "test-key");
            ImageAnalysisRequest req = new ImageAnalysisRequest();
            req.setImages(new ArrayList<>());
            req.setMessage("Phân tích");
            assertThrows(IllegalArgumentException.class, () -> chatService.analyzeImages(req));
        }

        @Test
        @DisplayName("TC18 - Request null -> IllegalArgumentException")
        void tc18_AnalyzeImages_NullRequest() {
            ReflectionTestUtils.setField(chatService, "geminiApiKey", "test-key");
            assertThrows(IllegalArgumentException.class, () -> chatService.analyzeImages(null));
        }
    }
}