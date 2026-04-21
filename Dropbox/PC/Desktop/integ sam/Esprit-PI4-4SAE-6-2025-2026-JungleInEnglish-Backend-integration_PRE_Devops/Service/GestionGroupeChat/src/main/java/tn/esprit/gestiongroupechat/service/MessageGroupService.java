package tn.esprit.gestiongroupechat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import tn.esprit.gestiongroupechat.CryptoUtil;
import tn.esprit.gestiongroupechat.Dto.MessageGroupDTO;
import tn.esprit.gestiongroupechat.entities.GroupChat;
import tn.esprit.gestiongroupechat.entities.MessageGroup;
import tn.esprit.gestiongroupechat.mapper.MessageGroupMapper;
import tn.esprit.gestiongroupechat.repository.GroupChatRepo;
import tn.esprit.gestiongroupechat.repository.MessageRepo;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageGroupService implements InMessageGroup {

    private final MessageRepo messageGroupRepo;
    private final GroupChatRepo groupChatRepo;
    private final MessageGroupMapper messageGroupMapper;

    @Override
    public MessageGroupDTO addMessage(MessageGroupDTO dto) {

        GroupChat groupChat = groupChatRepo.findById(dto.groupChatId())
                .orElseThrow(() -> new RuntimeException("GroupChat not found"));

        MessageGroup entity = messageGroupMapper.toEntity(dto);
        entity.setGroupChat(groupChat);

        // Encrypt AVANT save
        entity.setContent(CryptoUtil.encrypt(entity.getContent()));

        MessageGroup saved = messageGroupRepo.save(entity);

        // 🔥 DECRYPT avant retour
        String decrypted = CryptoUtil.decrypt(saved.getContent());

        return new MessageGroupDTO(
                saved.getIdMessageChat(),
                saved.getSender(),
                decrypted, // ici en clair
                saved.getGroupChat().getId(),
                saved.getDateSend(),
                saved.isDeleted()
        );
    }



    @Override
    public List<MessageGroupDTO> getAll() {
        return messageGroupRepo.findAll()
                .stream()
                .map(msg -> {

                    msg.setContent(CryptoUtil.decrypt(msg.getContent()));
                    return messageGroupMapper.toDTO(msg);
                })
                .collect(Collectors.toList());
    }


    @Override
    public MessageGroupDTO getById(Long id) {
        MessageGroup entity = messageGroupRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        return messageGroupMapper.toDTO(entity);
    }

    @Override
    public MessageGroupDTO update(Long id, MessageGroupDTO dto) {
        MessageGroup existing = messageGroupRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        existing.setContent(dto.content());
        existing.setSender(dto.sender());

        MessageGroup updated = messageGroupRepo.save(existing);
        return messageGroupMapper.toDTO(updated);
    }

    @Override
    public void softDelete(Long id) {
        MessageGroup message = messageGroupRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Message non trouvé"));

        message.setDeleted(true);
        messageGroupRepo.save(message);
    }

    @Override
    public void delete(Long id) {
        messageGroupRepo.deleteById(id);
    }


    @Override
    public List<MessageGroupDTO> getMessagesByGroupId(Long groupId) {
        GroupChat groupChat = groupChatRepo.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Groupe introuvable"));

        return messageGroupRepo.findByGroupChat(groupChat)
                .stream()
                .map(msg -> {
                    String decryptedContent = CryptoUtil.decrypt(msg.getContent()); // <- ici
                    System.out.println("Décrypté: " + decryptedContent); // pour debug
                    return new MessageGroupDTO(
                            msg.getIdMessageChat(),
                            msg.getSender(),
                            decryptedContent, // <- envoyer clair
                            msg.getGroupChat().getId(),
                            msg.getDateSend(),
                            msg.isDeleted()
                    );
                })
                .collect(Collectors.toList());
    }




}
