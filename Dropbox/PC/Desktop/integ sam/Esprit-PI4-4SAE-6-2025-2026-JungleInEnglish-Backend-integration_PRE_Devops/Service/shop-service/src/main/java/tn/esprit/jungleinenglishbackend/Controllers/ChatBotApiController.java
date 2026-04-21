package tn.esprit.jungleinenglishbackend.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.jungleinenglishbackend.Models.ChatBotDTO;
import tn.esprit.jungleinenglishbackend.Services.ShopChatBotService;

@RestController
@RequestMapping("/api/shop/bot")
@RequiredArgsConstructor
public class ChatBotApiController {

    private final ShopChatBotService chatBotService;

    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody ChatBotDTO request) {
        if (request.getMessage() == null || request.getMessage().isBlank()) {
            return ResponseEntity.badRequest().body("Message cannot be empty.");
        }
        String response = chatBotService.chatWithStoreContext(request.getMessage());
        return ResponseEntity.ok(response);
    }
}
