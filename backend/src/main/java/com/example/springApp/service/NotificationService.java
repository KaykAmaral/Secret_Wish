package com.example.springApp.service;

import com.example.springApp.exception.ResourceNotFoundException;
import com.example.springApp.model.Notification;
import com.example.springApp.model.User;
import com.example.springApp.repository.NotificationRepository;
import com.example.springApp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Cria uma notificacao persistida para leitura posterior no painel do usuario.
     */
    @Transactional
    public Notification createNotification(Long userId, String title, String content) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado para envio de notificacao"));

        Notification notification = new Notification();
        notification.setUsuario(user);
        notification.setTitulo(title);
        notification.setMensagem(content);
        notification.setDataCriacao(LocalDateTime.now());

        return notificationRepository.save(notification);
    }

    /**
     * Lista notificacoes mais recentes primeiro para exibir no dashboard.
     */
    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUsuarioIdOrderByDataCriacaoDesc(userId);
    }

    /**
     * Conta notificacoes pendentes sem carregar a lista inteira.
     */
    public long countUnreadNotifications(Long userId) {
        return notificationRepository.countByUsuarioIdAndLidaFalse(userId);
    }

    /**
     * Marca uma notificacao individual garantindo que ela pertence ao usuario.
     */
    @Transactional
    public Notification markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findByIdAndUsuarioId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificacao nao encontrada"));
        notification.setLida(true);
        return notificationRepository.save(notification);
    }

    /**
     * Marca todas as notificacoes pendentes do usuario em uma unica operacao de lote.
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> notifications = notificationRepository.findByUsuarioIdAndLidaFalse(userId);
        notifications.forEach(notification -> notification.setLida(true));
        notificationRepository.saveAll(notifications);
    }

    /**
     * Exclui uma notificacao somente quando ela pertence ao usuario autenticado.
     */
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findByIdAndUsuarioId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificacao nao encontrada"));
        notificationRepository.delete(notification);
    }

    /**
     * Remove todo o historico de notificacoes do usuario.
     */
    @Transactional
    public void deleteUserNotifications(Long userId) {
        notificationRepository.deleteByUsuarioId(userId);
    }

}
