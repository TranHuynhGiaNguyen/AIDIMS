package com.aidims.aidimsbackend.controller;

import com.aidims.aidimsbackend.dto.ChatRequest;
import com.aidims.aidimsbackend.dto.ImageAnalysisRequest;
import com.aidims.aidimsbackend.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatController - Unit Tests")
class ChatControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ChatService chatService;

    @InjectMocks
    private ChatController chatController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(chatController).build();
    }

    @Test
    @DisplayName("✅ sendMessage - Gửi tin nhắn và nhận phản hồi chẩn đoán thành công")
    void sendMessage_Success() throws Exception {
        ChatRequest req = new ChatRequest();
        req.setMessage("Đau ngực");

        when(chatService.getChatResponse("Đau ngực")).thenReturn("Chẩn đoán: Sơ bộ");

        mockMvc.perform(post("/api/chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Chẩn đoán: Sơ bộ"));
    }

    @Test
    @DisplayName("✅ analyzeImage - Gửi phân tích ảnh DICOM thành công")
    void analyzeImage_Success() throws Exception {
        ImageAnalysisRequest req = new ImageAnalysisRequest();
        ImageAnalysisRequest.ImageData img = new ImageAnalysisRequest.ImageData();
        img.setName("test.dcm");
        img.setType("image/jpeg");
        img.setData("base64data");
        img.setSize(1024L);

        List<ImageAnalysisRequest.ImageData> list = new ArrayList<>();
        list.add(img);
        req.setImages(list);
        req.setMessage("Phân tích");

        when(chatService.analyzeImages(any(ImageAnalysisRequest.class))).thenReturn("Kết luận: Bình thường");

        mockMvc.perform(post("/api/chat/analyze-image")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Kết luận: Bình thường"));
    }

    @Test
    @DisplayName("❌ analyzeImage - Báo lỗi 400 khi danh sách hình ảnh gửi lên bị rỗng")
    void analyzeImage_EmptyImages() throws Exception {
        ImageAnalysisRequest req = new ImageAnalysisRequest();
        req.setImages(new ArrayList<>()); // Rỗng

        mockMvc.perform(post("/api/chat/analyze-image")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    @DisplayName("✅ testGemini - Thực thi gọi thử API trực tiếp của Gemini")
    void testGemini_Success() throws Exception {
        when(chatService.testGeminiDirectly("đau ngực")).thenReturn("Gemini response");

        mockMvc.perform(get("/api/chat/test-gemini").param("message", "đau ngực"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ healthCheck - Endpoint kiểm tra tình trạng dịch vụ")
    void healthCheck_Success() throws Exception {
        mockMvc.perform(get("/api/chat/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("❌ Lỗi nghiệp vụ: Gửi tin nhắn trống phải trả về thông báo lỗi chi tiết")
    void sendMessage_BlankMessage_ReturnsErrorMessage() throws Exception {
        ChatRequest req = new ChatRequest();
        req.setMessage("   "); // Trống

        // Mong đợi kết quả JSON trả về chứa status là "error" hoặc thông điệp cảnh báo.
        // Thực tế nó vẫn trả về status "success", làm test case FAIL về mặt logic.
        mockMvc.perform(post("/api/chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(jsonPath("$.status").value("error"));
    }
}
