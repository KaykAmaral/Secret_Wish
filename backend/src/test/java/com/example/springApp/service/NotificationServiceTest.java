package com.example.springApp.service;

import com.example.springApp.exception.ResourceNotFoundException;
import com.example.springApp.model.Notification;
import com.example.springApp.model.User;
import com.example.springApp.repository.NotificationRepository;
import com.example.springApp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void createNotificationPersistsNotificationForUser() {
        User user = user(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(10L);
            return notification;
        });

        Notification saved = notificationService.createNotification(1L, "Titulo", "Mensagem");

        assertThat(saved.getId()).isEqualTo(10L);
        assertThat(saved.getUsuario()).isSameAs(user);
        assertThat(saved.getTitulo()).isEqualTo("Titulo");
        assertThat(saved.getMensagem()).isEqualTo("Mensagem");
        assertThat(saved.getDataCriacao()).isNotNull();
        assertThat(saved.isLida()).isFalse();
    }

    @Test
    void markAsReadOnlyUpdatesNotificationOwnedByUser() {
        Notification notification = Notification.builder()
                .id(20L)
                .usuario(user(1L))
                .titulo("Titulo")
                .mensagem("Mensagem")
                .lida(false)
                .build();

        when(notificationRepository.findByIdAndUsuarioId(20L, 1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);

        Notification saved = notificationService.markAsRead(20L, 1L);

        assertThat(saved.isLida()).isTrue();
        verify(notificationRepository).save(notification);
    }

    @Test
    void markAsReadRejectsNotificationFromAnotherUser() {
        when(notificationRepository.findByIdAndUsuarioId(20L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(20L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Notificacao nao encontrada");

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void markAllAsReadUsesBulkUpdate() {
        notificationService.markAllAsRead(1L);

        // Garante que a operacao continua em lote e nao volta a carregar todas as notificacoes.
        verify(notificationRepository).markAllUnreadAsRead(1L);
        verify(notificationRepository, never()).saveAll(any());
    }

    private User user(Long id) {
        return User.builder()
                .id(id)
                .nome("User " + id)
                .email("user" + id + "@example.com")
                .build();
    }
}
