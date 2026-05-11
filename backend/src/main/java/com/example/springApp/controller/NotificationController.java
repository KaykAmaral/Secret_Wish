package com.example.springApp.controller;

import com.example.springApp.dto.NotificationResponse;
import com.example.springApp.dto.UnreadCountResponse;
import com.example.springApp.mapper.ResponseMapper;
import com.example.springApp.security.AuthenticatedUser;
import com.example.springApp.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final ResponseMapper responseMapper;
    private final AuthenticatedUser authenticatedUser;

    public NotificationController(
            NotificationService notificationService,
            ResponseMapper responseMapper,
            AuthenticatedUser authenticatedUser
    ) {
        this.notificationService = notificationService;
        this.responseMapper = responseMapper;
        this.authenticatedUser = authenticatedUser;
    }

    @GetMapping
    public List<NotificationResponse> list(Authentication authentication) {
        Long userId = authenticatedUser.id(authentication);
        return notificationService.getUserNotifications(userId).stream()
                .map(responseMapper::toNotificationResponse)
                .toList();
    }

    @GetMapping("/unread-count")
    public UnreadCountResponse unreadCount(Authentication authentication) {
        Long userId = authenticatedUser.id(authentication);
        return new UnreadCountResponse(notificationService.countUnreadNotifications(userId));
    }

    @PatchMapping("/{notificationId}/read")
    public NotificationResponse markAsRead(
            @PathVariable Long notificationId,
            Authentication authentication
    ) {
        Long userId = authenticatedUser.id(authentication);
        return responseMapper.toNotificationResponse(notificationService.markAsRead(notificationId, userId));
    }

    @PatchMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllAsRead(Authentication authentication) {
        Long userId = authenticatedUser.id(authentication);
        notificationService.markAllAsRead(userId);
    }

    @DeleteMapping("/{notificationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long notificationId,
            Authentication authentication
    ) {
        Long userId = authenticatedUser.id(authentication);
        notificationService.deleteNotification(notificationId, userId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAll(Authentication authentication) {
        Long userId = authenticatedUser.id(authentication);
        notificationService.deleteUserNotifications(userId);
    }
}
