package tn.esprit.gestiongroupechat.Ollama;



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


    public String translate(String text, String targetLanguage) {
        String prompt = String.format(
                "Translate the following message to %s. Reply with only the translation, no explanation:\n\n%s",
                targetLanguage, text
        );
        return chatModel.call(prompt);
    }

    //  Répondre à une question
    public String answerQuestion(String question) {
        String prompt = String.format(
                "Answer the following question clearly and concisely. Reply only with the answer:\n\n%s",
                question
        );
        return chatModel.call(prompt);
    }
}
