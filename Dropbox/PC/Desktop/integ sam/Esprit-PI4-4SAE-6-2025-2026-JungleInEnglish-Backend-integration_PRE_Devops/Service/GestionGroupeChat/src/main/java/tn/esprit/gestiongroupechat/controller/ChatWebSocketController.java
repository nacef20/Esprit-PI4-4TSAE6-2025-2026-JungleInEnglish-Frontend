package tn.esprit.gestiongroupechat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import tn.esprit.gestiongroupechat.Dto.MessageGroupDTO;
import tn.esprit.gestiongroupechat.entities.MessageGroup;
import tn.esprit.gestiongroupechat.service.InMessageGroup;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final InMessageGroup messageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload MessageGroupDTO dto) {


        MessageGroupDTO saved = messageService.addMessage(dto);


        messagingTemplate.convertAndSend(
                "/topic/group/" + saved.groupChatId(),
                saved
        );
    }

}
