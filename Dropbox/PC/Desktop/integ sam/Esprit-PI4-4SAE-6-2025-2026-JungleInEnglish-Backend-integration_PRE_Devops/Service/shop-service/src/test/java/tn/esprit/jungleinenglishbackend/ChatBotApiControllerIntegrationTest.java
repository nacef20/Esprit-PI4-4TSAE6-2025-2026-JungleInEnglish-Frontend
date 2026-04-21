package tn.esprit.jungleinenglishbackend;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.jungleinenglishbackend.Services.ShopChatBotService;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ChatBotApiController.
 *
 * Loads the full Spring context but mocks the ShopChatBotService so that
 * tests remain fast (no real Ollama / DB calls).
 *
 * Security is bypassed via mock-mode headers (X-Mock-User-Id / X-Mock-User-Role),
 * consistent with the pattern used across other tests in this project.
 */
@SpringBootTest(properties = {"app.security.mock-mode=true"})
@AutoConfigureMockMvc
class ChatBotApiControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShopChatBotService chatBotService;

    // =========================================================================
    // Successful Chat Request
    // =========================================================================

    @Test
    @DisplayName("POST /api/shop/bot/chat — valid message returns 200 with AI reply")
    void testChat_ValidMessage_Returns200WithReply() throws Exception {
        // Arrange
        String aiReply = "I recommend 'Short Stories in English' — it's perfect for beginners!";
        when(chatBotService.chatWithStoreContext(anyString())).thenReturn(aiReply);

        // Act & Assert
        mockMvc.perform(post("/api/shop/bot/chat")
                        .header("X-Mock-User-Id", "user-001")
                        .header("X-Mock-User-Role", "ROLE_USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\": \"Do you have any books for beginners?\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Short Stories in English")));

        verify(chatBotService, times(1)).chatWithStoreContext("Do you have any books for beginners?");
    }

    @Test
    @DisplayName("POST /api/shop/bot/chat — admin can also use the bot")
    void testChat_AdminUser_Returns200() throws Exception {
        // Arrange
        when(chatBotService.chatWithStoreContext(anyString())).thenReturn("Here are the products...");

        // Act & Assert
        mockMvc.perform(post("/api/shop/bot/chat")
                        .header("X-Mock-User-Id", "admin-001")
                        .header("X-Mock-User-Role", "ROLE_ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\": \"List all products\"}"))
                .andExpect(status().isOk());
    }

    // =========================================================================
    // Validation: Empty / Blank Message
    // =========================================================================

    @Test
    @DisplayName("POST /api/shop/bot/chat — empty message returns 400 Bad Request")
    void testChat_EmptyMessage_Returns400() throws Exception {
        mockMvc.perform(post("/api/shop/bot/chat")
                        .header("X-Mock-User-Id", "user-001")
                        .header("X-Mock-User-Role", "ROLE_USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Message cannot be empty")));

        // Service must NOT be called for empty messages
        verify(chatBotService, never()).chatWithStoreContext(anyString());
    }

    @Test
    @DisplayName("POST /api/shop/bot/chat — blank message (spaces only) returns 400 Bad Request")
    void testChat_BlankMessage_Returns400() throws Exception {
        mockMvc.perform(post("/api/shop/bot/chat")
                        .header("X-Mock-User-Id", "user-001")
                        .header("X-Mock-User-Role", "ROLE_USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\": \"   \"}"))
                .andExpect(status().isBadRequest());

        verify(chatBotService, never()).chatWithStoreContext(anyString());
    }

    @Test
    @DisplayName("POST /api/shop/bot/chat — null message returns 400 Bad Request")
    void testChat_NullMessage_Returns400() throws Exception {
        mockMvc.perform(post("/api/shop/bot/chat")
                        .header("X-Mock-User-Id", "user-001")
                        .header("X-Mock-User-Role", "ROLE_USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\": null}"))
                .andExpect(status().isBadRequest());

        verify(chatBotService, never()).chatWithStoreContext(anyString());
    }

    // =========================================================================
    // Content-Type Validation
    // =========================================================================

    @Test
    @DisplayName("POST /api/shop/bot/chat — missing Content-Type returns 415 Unsupported Media Type")
    void testChat_MissingContentType_Returns415() throws Exception {
        mockMvc.perform(post("/api/shop/bot/chat")
                        .header("X-Mock-User-Id", "user-001")
                        .header("X-Mock-User-Role", "ROLE_USER")
                        .content("{\"message\": \"hello\"}"))
                .andExpect(status().isUnsupportedMediaType());
    }

    // =========================================================================
    // Service Passthrough — Response Shape
    // =========================================================================

    @Test
    @DisplayName("POST /api/shop/bot/chat — response body is the raw AI text (not JSON wrapped)")
    void testChat_ResponseIsRawText() throws Exception {
        String rawReply = "We have 6 books available.";
        when(chatBotService.chatWithStoreContext(anyString())).thenReturn(rawReply);

        mockMvc.perform(post("/api/shop/bot/chat")
                        .header("X-Mock-User-Id", "user-001")
                        .header("X-Mock-User-Role", "ROLE_USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\": \"how many books?\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(rawReply));
    }
}
