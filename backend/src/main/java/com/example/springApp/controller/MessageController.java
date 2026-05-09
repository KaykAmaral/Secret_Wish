package com.example.springApp.controller;

import com.example.springApp.dto.MessageResponse;
import com.example.springApp.dto.SendMessageRequest;
import com.example.springApp.dto.UnreadCountResponse;
import com.example.springApp.mapper.ResponseMapper;
import com.example.springApp.security.AuthenticatedUser;
import com.example.springApp.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MessageController {

    private final MessageService messageService;
    private final ResponseMapper responseMapper;
    private final AuthenticatedUser authenticatedUser;

    public MessageController(
            MessageService messageService,
            ResponseMapper responseMapper,
            AuthenticatedUser authenticatedUser
    ) {
        this.messageService = messageService;
        this.responseMapper = responseMapper;
        this.authenticatedUser = authenticatedUser;
    }

    @PostMapping("/groups/{groupId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse send(
            @PathVariable Long groupId,
            @Valid @RequestBody SendMessageRequest request,
            Authentication authentication
    ) {
        Long userId = authenticatedUser.id(authentication);
        return responseMapper.toMessageResponse(
                messageService.sendMessage(groupId, userId, request.destinatarioId(), request.conteudo()),
                userId
        );
    }

    @GetMapping("/groups/{groupId}/messages/{otherUserId}")
    public List<MessageResponse> conversation(
            @PathVariable Long groupId,
            @PathVariable Long otherUserId,
            Authentication authentication
    ) {
        Long userId = authenticatedUser.id(authentication);
        return messageService.getConversation(groupId, userId, otherUserId).stream()
                .map(message -> responseMapper.toMessageResponse(message, userId))
                .toList();
    }

    @PatchMapping("/groups/{groupId}/messages/{otherUserId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAsRead(
            @PathVariable Long groupId,
            @PathVariable Long otherUserId,
            Authentication authentication
    ) {
        Long userId = authenticatedUser.id(authentication);
        messageService.markConversationAsRead(groupId, userId, otherUserId);
    }

    @GetMapping("/messages/unread-count")
    public UnreadCountResponse unreadCount(Authentication authentication) {
        Long userId = authenticatedUser.id(authentication);
        return new UnreadCountResponse(messageService.countUnreadMessages(userId));
    }
}
