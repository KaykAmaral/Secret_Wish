package com.example.springApp.service;

import com.example.springApp.dto.RealtimeMessageNotification;
import com.example.springApp.dto.ChatSummaryResponse;
import com.example.springApp.dto.UnreadConversationCount;
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
import java.util.Map;
import java.util.stream.Collectors;

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

    /**
     * Envia mensagem apenas entre pares autorizados pelo sorteio e atualiza o destinatario em tempo real.
     */
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

    /**
     * Recupera a conversa entre duas pontas do sorteio quando o usuario tem permissao para acessa-la.
     */
    public List<Message> getConversation(Long groupId, Long userId, Long otherUserId) {
        if (!canExchangeMessages(groupId, userId, otherUserId)) {
            throw new ForbiddenException("Voce nao pode acessar esta conversa");
        }

        return messageRepository.findConversation(groupId, userId, otherUserId);
    }

    /**
     * Monta os resumos dos chats permitidos para o usuario apos o sorteio do grupo.
     */
    @Transactional(readOnly = true)
    public List<ChatSummaryResponse> getChatSummaries(Long groupId, Long userId) {
        if (!groupRepository.existsByIdAndMembros_Id(groupId, userId)) {
            if (!groupRepository.existsById(groupId)) {
                throw new ResourceNotFoundException("Grupo nao encontrado");
            }
            throw new ForbiddenException("Voce nao faz parte deste grupo");
        }

        List<Draw> draws = drawRepository.findUserDrawRelations(groupId, userId);
        List<Long> otherUserIds = draws.stream()
                .map(draw -> otherUser(draw, userId).getId())
                .toList();
        // Busca todos os badges em uma unica agregacao para evitar N+1 ao montar os cards de chat.
        Map<Long, Long> unreadByOtherUser = otherUserIds.isEmpty()
                ? Map.of()
                : messageRepository.countUnreadByConversationPartners(groupId, userId, otherUserIds).stream()
                        .collect(Collectors.toMap(
                                UnreadConversationCount::getOtherUserId,
                                UnreadConversationCount::getUnreadCount
                        ));

        return draws.stream()
                .map(draw -> toChatSummary(draw, groupId, userId, unreadByOtherUser))
                .toList();
    }

    /**
     * Marca como lidas apenas as mensagens recebidas pelo usuario nessa conversa.
     */
    @Transactional
    public void markConversationAsRead(Long groupId, Long userId, Long otherUserId) {
        if (!canExchangeMessages(groupId, userId, otherUserId)) {
            throw new ForbiddenException("Voce nao pode acessar esta conversa");
        }

        // Atualizacao em lote evita carregar e salvar todo o historico apenas para mudar flag de leitura.
        messageRepository.markConversationReceivedMessagesAsRead(groupId, userId, otherUserId);
        Long unreadCount = countUnreadMessages(userId);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                realtimeNotificationService.notifyUnreadCount(userId, unreadCount);
            }
        });
    }

    /**
     * Calcula o contador exibido no frontend e enviado via WebSocket.
     */
    public Long countUnreadMessages(Long userId) {
        return messageRepository.countByDestinatarioIdAndLidaFalse(userId);
    }

    /**
     * Confirma que os dois usuarios formam um par direto do sorteio em qualquer direcao.
     */
    private boolean canExchangeMessages(Long groupId, Long userId, Long otherUserId) {
        return drawRepository.existsByGrupo_IdAndRemetente_IdAndDestinatario_Id(groupId, userId, otherUserId)
                || drawRepository.existsByGrupo_IdAndRemetente_IdAndDestinatario_Id(groupId, otherUserId, userId);
    }

    /**
     * Valida participacao pelo id para nao depender do estado da entidade JPA.
     */
    private boolean isMember(Group group, Long userId) {
        return group.getMembros().stream()
                .anyMatch(member -> member.getId().equals(userId));
    }

    /**
     * Define como cada conversa deve aparecer para preservar anonimato quando necessario.
     */
    private ChatSummaryResponse toChatSummary(Draw draw, Long groupId, Long userId, Map<Long, Long> unreadByOtherUser) {
        boolean userIsGiver = draw.getRemetente().getId().equals(userId);
        User otherUser = otherUser(draw, userId);
        Long unreadCount = unreadByOtherUser.getOrDefault(otherUser.getId(), 0L);

        return new ChatSummaryResponse(
                groupId,
                otherUser.getId(),
                userIsGiver ? otherUser.getNome() : "amigo secreto",
                !userIsGiver,
                unreadCount
        );
    }

    private User otherUser(Draw draw, Long userId) {
        boolean userIsGiver = draw.getRemetente().getId().equals(userId);
        return userIsGiver ? draw.getDestinatario() : draw.getRemetente();
    }

}
