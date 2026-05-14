package com.example.springApp.service;

import com.example.springApp.dto.RealtimeMessageNotification;
import com.example.springApp.dto.ChatSummaryResponse;
import com.example.springApp.exception.ForbiddenException;
import com.example.springApp.exception.ResourceNotFoundException;
import com.example.springApp.model.Draw;
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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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

    @Autowired
    private RealtimeNotificationService realtimeNotificationService;

    @Transactional
    public Message sendMessage(Long groupId, Long senderId, Long receiverId, String content) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo nao encontrado"));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Remetente nao encontrado"));

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Destinatario nao encontrado"));

        if (!isMember(group, senderId)) {
            throw new ForbiddenException("Voce nao faz parte deste grupo");
        }

        if (!isMember(group, receiverId)) {
            throw new ForbiddenException("Destinatario nao faz parte deste grupo");
        }

        if (!canExchangeMessages(groupId, senderId, receiverId)) {
            throw new ForbiddenException("Mensagens so podem ser trocadas entre pares do sorteio");
        }

        // Mensagens sao privadas entre pares do sorteio e sempre anonimas para o destinatario.
        Message message = new Message();
        message.setGrupo(group);
        message.setRemetente(sender);
        message.setDestinatario(receiver);
        message.setConteudo(content);
        message.setDataEnvio(LocalDateTime.now());
        message.setAnonima(true);

        Message savedMessage = messageRepository.save(message);
        Long unreadCount = countUnreadMessages(receiverId);
        RealtimeMessageNotification notification = new RealtimeMessageNotification(
                group.getId(),
                savedMessage.getId(),
                "amigo secreto",
                savedMessage.getConteudo(),
                savedMessage.getDataEnvio(),
                unreadCount
        );
        // WebSocket so notifica depois que a mensagem foi persistida com sucesso.
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                realtimeNotificationService.notifyNewMessage(notification, receiverId);
            }
        });

        return savedMessage;
    }

    public List<Message> getConversation(Long groupId, Long userId, Long otherUserId) {
        if (!canExchangeMessages(groupId, userId, otherUserId)) {
            throw new ForbiddenException("Voce nao pode acessar esta conversa");
        }

        return messageRepository.findConversation(groupId, userId, otherUserId);
    }

    @Transactional(readOnly = true)
    public List<ChatSummaryResponse> getChatSummaries(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo nao encontrado"));
        if (!isMember(group, userId)) {
            throw new ForbiddenException("Voce nao faz parte deste grupo");
        }

        return drawRepository.findUserDrawRelations(groupId, userId).stream()
                .map(draw -> toChatSummary(draw, groupId, userId))
                .toList();
    }

    @Transactional
    public void markConversationAsRead(Long groupId, Long userId, Long otherUserId) {
        List<Message> messages = getConversation(groupId, userId, otherUserId);
        messages.stream()
                .filter(message -> message.getDestinatario().getId().equals(userId))
                .forEach(message -> message.setLida(true));
        messageRepository.saveAll(messages);
        Long unreadCount = countUnreadMessages(userId);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                realtimeNotificationService.notifyUnreadCount(userId, unreadCount);
            }
        });
    }

    public Long countUnreadMessages(Long userId) {
        return messageRepository.countByDestinatarioIdAndLidaFalse(userId);
    }

    private boolean canExchangeMessages(Long groupId, Long userId, Long otherUserId) {
        return drawRepository.existsByGrupo_IdAndRemetente_IdAndDestinatario_Id(groupId, userId, otherUserId)
                || drawRepository.existsByGrupo_IdAndRemetente_IdAndDestinatario_Id(groupId, otherUserId, userId);
    }

    private boolean isMember(Group group, Long userId) {
        return group.getMembros().stream()
                .anyMatch(member -> member.getId().equals(userId));
    }

    private ChatSummaryResponse toChatSummary(Draw draw, Long groupId, Long userId) {
        boolean userIsGiver = draw.getRemetente().getId().equals(userId);
        User otherUser = userIsGiver ? draw.getDestinatario() : draw.getRemetente();
        // Quando o usuario foi tirado, a outra ponta continua oculta como "amigo secreto".
        Long unreadCount = messageRepository.countByGrupoIdAndRemetenteIdAndDestinatarioIdAndLidaFalse(
                groupId,
                otherUser.getId(),
                userId
        );

        return new ChatSummaryResponse(
                groupId,
                otherUser.getId(),
                userIsGiver ? otherUser.getNome() : "amigo secreto",
                !userIsGiver,
                unreadCount
        );
    }

}
