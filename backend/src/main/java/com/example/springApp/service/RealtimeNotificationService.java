package com.example.springApp.service;

import com.example.springApp.dto.RealtimeMessageNotification;
import com.example.springApp.dto.UnreadCountResponse;
import com.example.springApp.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class RealtimeNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RealtimeNotificationService.class);

    private final SimpMessagingTemplate messagingTemplate;

    public RealtimeNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Converte uma entidade Message em payload minimo antes de enviar ao destinatario.
     */
    public void notifyNewMessage(Message message, Long unreadCount) {
        notifyNewMessage(new RealtimeMessageNotification(
                        message.getGrupo().getId(),
                        message.getId(),
                        "amigo secreto",
                        message.getConteudo(),
                        message.getDataEnvio(),
                        unreadCount
                ),
                message.getDestinatario().getId()
        );
    }

    /**
     * Entrega a nova mensagem no canal privado do usuario e sincroniza o contador de nao lidas.
     */
    public void notifyNewMessage(RealtimeMessageNotification notification, Long recipientId) {
        try {
            messagingTemplate.convertAndSendToUser(
                    recipientId.toString(),
                    "/queue/messages",
                    notification
            );
            notifyUnreadCount(recipientId, notification.unreadCount());
        } catch (RuntimeException ex) {
            LOGGER.error("Falha ao enviar notificacao WebSocket de nova mensagem para usuario {}", recipientId, ex);
        }
    }

    /**
     * Publica o total atual de mensagens nao lidas para atualizar badges no frontend.
     */
    public void notifyUnreadCount(Long userId, Long unreadCount) {
        try {
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/unread-count",
                    new UnreadCountResponse(unreadCount)
            );
        } catch (RuntimeException ex) {
            LOGGER.error("Falha ao enviar contador de mensagens nao lidas para usuario {}", userId, ex);
        }
    }

    /**
     * Avisa quem tirou o usuario quando a wishlist dele muda.
     */
    public void notifyWishlistUpdate(Long targetUserId, Long groupId) {
        try {
            messagingTemplate.convertAndSendToUser(
                    targetUserId.toString(),
                    "/queue/wishlist-update",
                    new WishlistUpdateNotification(groupId)
            );
        } catch (RuntimeException ex) {
            LOGGER.error("Falha ao enviar notificacao de wishlist para usuario {}", targetUserId, ex);
        }
    }

    public record WishlistUpdateNotification(Long groupId) {}
}
