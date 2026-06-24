package com.aidims.aidimsbackend.service;

import com.aidims.aidimsbackend.dto.ImageAnalysisRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    @Test
    @DisplayName("✅ getChatResponse - Hoạt động bình thường với cơ chế Fallback nội bộ khi không có API key thực")
    void getChatResponse_FallbackSuccess() {
        ReflectionTestUtils.setField(chatService, "geminiApiKey", "");
        ReflectionTestUtils.setField(chatService, "openaiApiKey", "");

        String response = chatService.getChatResponse("đau ngực và khó thở");

        assertNotNull(response);
        assertTrue(response.contains("PHÂN TÍCH TRIỆU CHỨNG"));
        assertTrue(response.contains("ĐAU NGỰC"));
        assertTrue(response.contains("KHÓ THỞ"));
    }

    @Test
    @DisplayName("✅ getChatResponse - Nhận biết đúng triệu chứng tiêu hóa và đưa ra khuyến nghị")
    void getChatResponse_SymptomGastrointestinal() {
        ReflectionTestUtils.setField(chatService, "geminiApiKey", "");
        ReflectionTestUtils.setField(chatService, "openaiApiKey", "");

        String response = chatService.getChatResponse("bị đau bụng và buồn nôn");

        assertNotNull(response);
        assertTrue(response.contains("ĐAU BỤNG"));
        assertTrue(response.contains("BUỒN NÔN"));
    }

    @Test
    @DisplayName("✅ getChatResponse - Đưa ra cảnh báo khẩn cấp khi triệu chứng nguy hiểm")
    void getChatResponse_EmergencyAnalysis() {
        ReflectionTestUtils.setField(chatService, "geminiApiKey", "");
        ReflectionTestUtils.setField(chatService, "openaiApiKey", "");

        String response = chatService.getChatResponse("co giật");

        assertNotNull(response);
        assertTrue(response.contains("CO GIẬT"));
        assertTrue(response.contains("KHẨN CẤP"));
    }

    @Test
    @DisplayName("❌ testGeminiDirectly - Ném ngoại lệ khi API key chưa được cấu hình")
    void testGeminiDirectly_MissingApiKey() {
        ReflectionTestUtils.setField(chatService, "geminiApiKey", "");

        assertThrows(RuntimeException.class, () -> chatService.testGeminiDirectly("Hello"));
    }

    @Test
    @DisplayName("❌ analyzeImages - Ném ngoại lệ khi gọi AI Vision mà không có API key")
    void analyzeImages_MissingApiKey() {
        ReflectionTestUtils.setField(chatService, "geminiApiKey", "");
        ImageAnalysisRequest req = new ImageAnalysisRequest();

        assertThrows(RuntimeException.class, () -> chatService.analyzeImages(req));
    }

    @Test
    @DisplayName("❌ Lỗi nghiệp vụ: Service không được phép phân tích hình ảnh khi danh sách hình ảnh gửi lên bị rỗng")
    void analyzeImages_EmptyImagesList_ThrowsException() {
        ImageAnalysisRequest req = new ImageAnalysisRequest();
        req.setImages(new ArrayList<>());
        req.setMessage("Phân tích");

        assertThrows(IllegalArgumentException.class, () -> chatService.analyzeImages(req),
            "Danh sách ảnh trống phải ném ra IllegalArgumentException"
        );
    }
}
