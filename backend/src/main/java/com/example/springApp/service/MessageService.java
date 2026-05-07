package com.example.springApp.service;

import com.example.springApp.exception.ForbiddenException;
import com.example.springApp.exception.ResourceNotFoundException;
import com.example.springApp.model.Group;
import com.example.springApp.model.Message;
import com.example.springApp.model.User;
import com.example.springApp.repository.GroupRepository;
import com.example.springApp.repository.MessageRepository;
import com.example.springApp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Message sendMessage(Long groupId, Long senderId, String content, boolean isAnonymous) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo nao encontrado"));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Remetente nao encontrado"));

        if (!group.getMembros().contains(sender)) {
            throw new ForbiddenException("Voce nao faz parte deste grupo");
        }

        Message message = new Message();
        message.setGrupo(group);
        message.setRemetente(sender);
        message.setConteudo(content);
        message.setDataEnvio(LocalDateTime.now());
        message.setAnonimo(isAnonymous);

        return messageRepository.save(message);
    }

    public List<Message> getGroupMessages(Long groupId) {
        return messageRepository.findByGrupo_IdOrderByDataEnvioAsc(groupId);
    }

}
