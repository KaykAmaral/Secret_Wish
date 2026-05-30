package com.example.springApp.service;

import com.example.springApp.exception.ResourceNotFoundException;
import com.example.springApp.model.Notification;
import com.example.springApp.model.User;
import com.example.springApp.repository.NotificationRepository;
import com.example.springApp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private static final int DEFAULT_NOTIFICATION_LIMIT = 50;
    private static final int MAX_NOTIFICATION_LIMIT = 100;

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
     * Lista uma pagina limitada de notificacoes para manter memoria e payload previsiveis.
     */
    public List<Notification> getUserNotifications(Long userId, int page, int size) {
        // Limite defensivo: cliente pode pedir menos, mas nao pode forcar carga excessiva.
        Pageable pageable = PageRequest.of(Math.max(0, page), safeSize(size));
        return notificationRepository.findByUsuarioIdOrderByDataCriacaoDesc(userId, pageable);
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
        notificationRepository.markAllUnreadAsRead(userId);
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

    private int safeSize(int size) {
        if (size <= 0) {
            return DEFAULT_NOTIFICATION_LIMIT;
        }
        return Math.min(size, MAX_NOTIFICATION_LIMIT);
    }

}
