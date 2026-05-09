package com.example.springApp.service;

import com.example.springApp.exception.ForbiddenException;
import com.example.springApp.exception.ResourceNotFoundException;
import com.example.springApp.model.Group;
import com.example.springApp.model.Message;
import com.example.springApp.model.User;
import com.example.springApp.repository.DrawRepository;
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

    @Autowired
    private DrawRepository drawRepository;

    @Transactional
    public Message sendMessage(Long groupId, Long senderId, Long receiverId, String content) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo nao encontrado"));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Remetente nao encontrado"));

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Destinatario nao encontrado"));

        if (!group.getMembros().contains(sender)) {
            throw new ForbiddenException("Voce nao faz parte deste grupo");
        }

        if (!group.getMembros().contains(receiver)) {
            throw new ForbiddenException("Destinatario nao faz parte deste grupo");
        }

        if (!canExchangeMessages(groupId, senderId, receiverId)) {
            throw new ForbiddenException("Mensagens so podem ser trocadas entre pares do sorteio");
        }

        Message message = new Message();
        message.setGrupo(group);
        message.setRemetente(sender);
        message.setDestinatario(receiver);
        message.setConteudo(content);
        message.setDataEnvio(LocalDateTime.now());
        message.setAnonima(true);

        return messageRepository.save(message);
    }

    public List<Message> getConversation(Long groupId, Long userId, Long otherUserId) {
        if (!canExchangeMessages(groupId, userId, otherUserId)) {
            throw new ForbiddenException("Voce nao pode acessar esta conversa");
        }

        return messageRepository.findConversation(groupId, userId, otherUserId);
    }

    @Transactional
    public void markConversationAsRead(Long groupId, Long userId, Long otherUserId) {
        List<Message> messages = getConversation(groupId, userId, otherUserId);
        messages.stream()
                .filter(message -> message.getDestinatario().getId().equals(userId))
                .forEach(message -> message.setLida(true));
        messageRepository.saveAll(messages);
    }

    public Long countUnreadMessages(Long userId) {
        return messageRepository.countByDestinatarioIdAndLidaFalse(userId);
    }

    private boolean canExchangeMessages(Long groupId, Long userId, Long otherUserId) {
        return drawRepository.existsByGrupo_IdAndRemetente_IdAndDestinatario_Id(groupId, userId, otherUserId)
                || drawRepository.existsByGrupo_IdAndRemetente_IdAndDestinatario_Id(groupId, otherUserId, userId);
    }

}
