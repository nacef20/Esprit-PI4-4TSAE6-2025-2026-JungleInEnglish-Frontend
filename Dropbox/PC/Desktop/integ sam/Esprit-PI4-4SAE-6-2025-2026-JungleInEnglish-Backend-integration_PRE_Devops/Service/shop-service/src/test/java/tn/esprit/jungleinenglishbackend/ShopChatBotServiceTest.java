package tn.esprit.jungleinenglishbackend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;
import tn.esprit.jungleinenglishbackend.Models.Product;
import tn.esprit.jungleinenglishbackend.Services.ProductService;
import tn.esprit.jungleinenglishbackend.Services.ShopChatBotService;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ShopChatBotService.
 * Uses Mockito to mock both the AI ChatModel and ProductService,
 * so tests run fast without requiring Ollama or a real database.
 */
@ExtendWith(MockitoExtension.class)
class ShopChatBotServiceTest {

    @Mock
    private ChatModel chatModel;

    @Mock
    private ProductService productService;

    @InjectMocks
    private ShopChatBotService chatBotService;

    private Product activeProduct;
    private Product inactiveProduct;

    @BeforeEach
    void setUp() {
        activeProduct = new Product();
        activeProduct.setId(1);
        activeProduct.setTitle("Short Stories in English");
        activeProduct.setCategory("Light Novel");
        activeProduct.setPrice(9.99);
        activeProduct.setStock(50);
        activeProduct.setDiscount(10);
        activeProduct.setIsActive(true);

        inactiveProduct = new Product();
        inactiveProduct.setId(2);
        inactiveProduct.setTitle("Discontinued Grammar Book");
        inactiveProduct.setCategory("Grammar");
        inactiveProduct.setPrice(5.00);
        inactiveProduct.setStock(0);
        inactiveProduct.setDiscount(0);
        inactiveProduct.setIsActive(false);
    }

    // =========================================================================
    // Happy Path — Active Products Present
    // =========================================================================

    @Test
    @DisplayName("chatWithStoreContext — should call AI with active products in context")
    void testChatWithStoreContext_WithActiveProducts_CallsAI() {
        // Arrange
        when(productService.getAllProducts())
                .thenReturn(List.of(activeProduct, inactiveProduct));
        when(chatModel.call(anyString())).thenReturn("Yes, we have Short Stories in English!");

        // Act
        String result = chatBotService.chatWithStoreContext("Do you have any light novels?");

        // Assert
        assertThat(result).isEqualTo("Yes, we have Short Stories in English!");
        verify(chatModel, times(1)).call(anyString());
    }

    @Test
    @DisplayName("chatWithStoreContext — prompt must include active product name but NOT inactive product")
    void testChatWithStoreContext_PromptContainsOnlyActiveProducts() {
        // Arrange
        when(productService.getAllProducts())
                .thenReturn(List.of(activeProduct, inactiveProduct));
        when(chatModel.call(anyString())).thenReturn("Some AI reply");

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        chatBotService.chatWithStoreContext("What books are available?");

        // Assert: capture the exact prompt sent to the AI
        verify(chatModel).call(promptCaptor.capture());
        String capturedPrompt = promptCaptor.getValue();

        assertThat(capturedPrompt).contains("Short Stories in English");
        assertThat(capturedPrompt).doesNotContain("Discontinued Grammar Book"); // inactive filtered out
        assertThat(capturedPrompt).contains("What books are available?");       // user message included
    }

    @Test
    @DisplayName("chatWithStoreContext — prompt must contain catalog section header")
    void testChatWithStoreContext_PromptContainsCatalogHeader() {
        // Arrange
        when(productService.getAllProducts()).thenReturn(List.of(activeProduct));
        when(chatModel.call(anyString())).thenReturn("reply");

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        chatBotService.chatWithStoreContext("hello");

        // Assert: system role + catalog markers present
        verify(chatModel).call(promptCaptor.capture());
        String prompt = promptCaptor.getValue();
        assertThat(prompt).contains("Jungle In English");
        assertThat(prompt).contains("Product Catalog");
    }

    // =========================================================================
    // Empty Catalog Edge Case
    // =========================================================================

    @Test
    @DisplayName("chatWithStoreContext — when no active products, prompt mentions empty catalog")
    void testChatWithStoreContext_NoActiveProducts_PromptMentionsEmptyCatalog() {
        // Arrange: only inactive products in DB
        when(productService.getAllProducts()).thenReturn(List.of(inactiveProduct));
        when(chatModel.call(anyString())).thenReturn("Sorry, no products right now.");

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        chatBotService.chatWithStoreContext("What can I buy?");

        // Assert
        verify(chatModel).call(promptCaptor.capture());
        assertThat(promptCaptor.getValue()).contains("no active products");
    }

    @Test
    @DisplayName("chatWithStoreContext — when DB is empty, prompt mentions empty catalog")
    void testChatWithStoreContext_EmptyDB_PromptMentionsEmptyCatalog() {
        // Arrange
        when(productService.getAllProducts()).thenReturn(Collections.emptyList());
        when(chatModel.call(anyString())).thenReturn("Sorry, no products right now.");

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        chatBotService.chatWithStoreContext("What can I buy?");

        // Assert
        verify(chatModel).call(promptCaptor.capture());
        assertThat(promptCaptor.getValue()).contains("no active products");
    }

    // =========================================================================
    // Response Passthrough
    // =========================================================================

    @Test
    @DisplayName("chatWithStoreContext — should return exactly what the AI model responds")
    void testChatWithStoreContext_ReturnsAIResponse() {
        // Arrange
        String expectedAIResponse = "We recommend 'Short Stories in English' for beginners.";
        when(productService.getAllProducts()).thenReturn(List.of(activeProduct));
        when(chatModel.call(anyString())).thenReturn(expectedAIResponse);

        // Act
        String result = chatBotService.chatWithStoreContext("recommend something");

        // Assert
        assertThat(result).isEqualTo(expectedAIResponse);
    }

    @Test
    @DisplayName("chatWithStoreContext — should call ProductService exactly once per request")
    void testChatWithStoreContext_CallsProductServiceOnce() {
        // Arrange
        when(productService.getAllProducts()).thenReturn(List.of(activeProduct));
        when(chatModel.call(anyString())).thenReturn("ok");

        // Act
        chatBotService.chatWithStoreContext("any question");

        // Assert
        verify(productService, times(1)).getAllProducts();
    }
}
