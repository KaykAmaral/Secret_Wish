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
}
