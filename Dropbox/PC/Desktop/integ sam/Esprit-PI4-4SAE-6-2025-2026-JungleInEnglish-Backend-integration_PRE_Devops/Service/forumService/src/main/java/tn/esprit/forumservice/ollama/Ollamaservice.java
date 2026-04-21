package tn.esprit.forumservice.ollama;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Ollamaservice {


    @Autowired
    private ChatModel chatModel;

    public String askOllama(String prompt) {
        return chatModel.call(prompt);
    }





    public String correctText(String text) {
        String prompt = "Corrige ce texte sans changer le sens :\n" + text;
        return chatModel.call(prompt);
    }

    // 🔹 Résumé
    public String summarizeText(String text) {
        String prompt = "Résume ce texte en 2 phrases simples :\n" + text;
        return chatModel.call(prompt);
    }

    // 🔹 Assistant (général)
    public String askAssistant(String text) {
        String prompt = "Aide-moi à améliorer ce texte pour un forum :\n" + text;
        return chatModel.call(prompt);
    }

    // 🔹 Traduction
    public String translate(String text, String lang) {
        String prompt = "Traduis ce texte en " + lang + " sans explication :\n" + text;
        return chatModel.call(prompt);
    }
}
