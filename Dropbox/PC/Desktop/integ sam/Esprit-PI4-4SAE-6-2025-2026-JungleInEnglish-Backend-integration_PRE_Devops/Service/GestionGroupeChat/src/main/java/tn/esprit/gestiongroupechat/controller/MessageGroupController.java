package tn.esprit.gestiongroupechat.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.gestiongroupechat.Dto.MessageGroupDTO;
import tn.esprit.gestiongroupechat.Ollama.Ollamaservice;
import tn.esprit.gestiongroupechat.service.InMessageGroup;

import java.util.List;

@RestController
@RequestMapping("/messageGroup")
@RequiredArgsConstructor
public class MessageGroupController {
    private final InMessageGroup messageService;
    private final Ollamaservice ollamaService;

    @PostMapping("/add")
    public ResponseEntity<MessageGroupDTO> addMessage(@RequestBody @Valid MessageGroupDTO dto) {
        MessageGroupDTO created = messageService.addMessage(dto);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/group/{id}")
    public List<MessageGroupDTO> getMessagesByGroup(@PathVariable Long id) {
        return messageService.getMessagesByGroupId(id);
    }

    @GetMapping("/all")
    public ResponseEntity<List<MessageGroupDTO>> getAll() {
        return ResponseEntity.ok(messageService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MessageGroupDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(messageService.getById(id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<MessageGroupDTO> update(@PathVariable Long id,
                                                  @RequestBody @Valid MessageGroupDTO dto) {
        return ResponseEntity.ok(messageService.update(id, dto));
    }

    @PutMapping("/delete/{id}")
    public ResponseEntity<String> softDeleteMessage(@PathVariable Long id) {
        messageService.softDelete(id);
        return ResponseEntity.ok("Message supprimé (soft delete)");
    }

    // In your Controller
    @PostMapping("/translate")
    public ResponseEntity<String> translateMessage(@RequestBody TranslateDTO dto) {
        if (dto.getText() == null || dto.getLanguage() == null) {
            return ResponseEntity.badRequest().body("text and language are required");
        }
        String result = ollamaService.translate(dto.getText(), dto.getLanguage());
        return ResponseEntity.ok(result);
    }

    public static class TranslateDTO {
        private String text;
        private String language;
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
    }

    @PostMapping("/ask")
    public ResponseEntity<String> askQuestion(@RequestBody QuestionDTO dto) {
        if (dto.getQuestion() == null || dto.getQuestion().isBlank()) {
            return ResponseEntity.badRequest().body("Le champ 'question' est requis");
        }
        String result = ollamaService.answerQuestion(dto.getQuestion());
        return ResponseEntity.ok(result);
    }

    static class QuestionDTO {
        private String question;

        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
    }

}
