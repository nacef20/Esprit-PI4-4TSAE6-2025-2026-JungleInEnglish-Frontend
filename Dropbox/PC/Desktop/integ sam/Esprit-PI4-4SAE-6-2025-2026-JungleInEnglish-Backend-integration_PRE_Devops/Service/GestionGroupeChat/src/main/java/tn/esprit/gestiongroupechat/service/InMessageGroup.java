package tn.esprit.gestiongroupechat.service;

import tn.esprit.gestiongroupechat.Dto.MessageGroupDTO;

import java.util.List;

public interface InMessageGroup {

    MessageGroupDTO addMessage(MessageGroupDTO dto);

    List<MessageGroupDTO> getAll();

    MessageGroupDTO getById(Long id);

    MessageGroupDTO update(Long id, MessageGroupDTO dto);

    void delete(Long id);

    List<MessageGroupDTO> getMessagesByGroupId(Long groupId);
    public void softDelete(Long id);
}
