package com.example.springApp.service;

import com.example.springApp.dto.RealtimeMessageNotification;
import com.example.springApp.dto.UnreadCountResponse;
import com.example.springApp.model.Group;
import com.example.springApp.model.Message;
import com.example.springApp.model.User;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RealtimeNotificationServiceTest {

    @Test
    void notifyNewMessageSendsMessageAndUnreadCountToRecipient() {
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        RealtimeNotificationService service = new RealtimeNotificationService(messagingTemplate);
        RealtimeMessageNotification notification = new RealtimeMessageNotification(
                10L,
                50L,
                "amigo secreto",
                "Mensagem",
                LocalDateTime.of(2026, 5, 30, 12, 0),
                3L
        );

        service.notifyNewMessage(notification, 2L);

        verify(messagingTemplate).convertAndSendToUser("2", "/queue/messages", notification);
        ArgumentCaptor<UnreadCountResponse> unreadCaptor = ArgumentCaptor.forClass(UnreadCountResponse.class);
        verify(messagingTemplate).convertAndSendToUser(eq("2"), eq("/queue/unread-count"), unreadCaptor.capture());
        assertThat(unreadCaptor.getValue().unreadCount()).isEqualTo(3L);
    }

    @Test
    void notifyNewMessageBuildsPayloadFromEntity() {
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        RealtimeNotificationService service = new RealtimeNotificationService(messagingTemplate);
        User receiver = User.builder().id(2L).nome("Bruno").build();
        Message message = Message.builder()
                .id(50L)
                .grupo(Group.builder().id(10L).build())
                .destinatario(receiver)
                .conteudo("Mensagem")
                .dataEnvio(LocalDateTime.of(2026, 5, 30, 12, 0))
                .build();

        service.notifyNewMessage(message, 4L);

        ArgumentCaptor<RealtimeMessageNotification> captor = ArgumentCaptor.forClass(RealtimeMessageNotification.class);
        verify(messagingTemplate).convertAndSendToUser(eq("2"), eq("/queue/messages"), captor.capture());
        assertThat(captor.getValue().groupId()).isEqualTo(10L);
        assertThat(captor.getValue().messageId()).isEqualTo(50L);
        assertThat(captor.getValue().unreadCount()).isEqualTo(4L);
    }

    @Test
    void notifyWishlistUpdateSendsGroupIdAndSwallowsMessagingFailure() {
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        RealtimeNotificationService service = new RealtimeNotificationService(messagingTemplate);

        service.notifyWishlistUpdate(2L, 10L);

        ArgumentCaptor<RealtimeNotificationService.WishlistUpdateNotification> captor =
                ArgumentCaptor.forClass(RealtimeNotificationService.WishlistUpdateNotification.class);
        verify(messagingTemplate).convertAndSendToUser(eq("2"), eq("/queue/wishlist-update"), captor.capture());
        assertThat(captor.getValue().groupId()).isEqualTo(10L);

        doThrow(new RuntimeException("broker down"))
                .when(messagingTemplate)
                .convertAndSendToUser(eq("3"), eq("/queue/wishlist-update"), any());

        assertThatCode(() -> service.notifyWishlistUpdate(3L, 10L))
                .doesNotThrowAnyException();
    }
}
