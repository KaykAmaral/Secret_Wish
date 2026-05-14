package com.example.springApp.controller;

import com.example.springApp.dto.ChatSummaryResponse;
import com.example.springApp.dto.MessageResponse;
import com.example.springApp.dto.SendMessageRequest;
import com.example.springApp.dto.UnreadCountResponse;
import com.example.springApp.mapper.ResponseMapper;
import com.example.springApp.security.AuthenticatedUser;
import com.example.springApp.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Mensagens", description = "Mensagens privadas entre remetente e destinatario dentro de um grupo.")
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
    @Operation(summary = "Enviar mensagem", description = "Envia mensagem privada para outro participante do grupo.")
    @ApiResponse(responseCode = "201", description = "Mensagem enviada")
    public MessageResponse send(
            @Parameter(description = "ID do grupo") @PathVariable Long groupId,
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
    @Operation(summary = "Consultar conversa", description = "Lista mensagens entre o usuario autenticado e outro participante.")
    public List<MessageResponse> conversation(
            @Parameter(description = "ID do grupo") @PathVariable Long groupId,
            @Parameter(description = "ID do outro participante") @PathVariable Long otherUserId,
            Authentication authentication
    ) {
        Long userId = authenticatedUser.id(authentication);
        return messageService.getConversation(groupId, userId, otherUserId).stream()
                .map(message -> responseMapper.toMessageResponse(message, userId))
                .toList();
    }

    @GetMapping("/groups/{groupId}/messages/chats")
    @Operation(
            summary = "Listar conversas do grupo",
            description = "Retorna as conversas permitidas para o usuario autenticado apos o sorteio."
    )
    public List<ChatSummaryResponse> chats(
            @Parameter(description = "ID do grupo") @PathVariable Long groupId,
            Authentication authentication
    ) {
        Long userId = authenticatedUser.id(authentication);
        return messageService.getChatSummaries(groupId, userId);
    }

    @PatchMapping("/groups/{groupId}/messages/{otherUserId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Marcar conversa como lida")
    @ApiResponse(responseCode = "204", description = "Mensagens marcadas como lidas")
    public void markAsRead(
            @Parameter(description = "ID do grupo") @PathVariable Long groupId,
            @Parameter(description = "ID do outro participante") @PathVariable Long otherUserId,
            Authentication authentication
    ) {
        Long userId = authenticatedUser.id(authentication);
        messageService.markConversationAsRead(groupId, userId, otherUserId);
    }

    @GetMapping("/messages/unread-count")
    @Operation(summary = "Contar mensagens nao lidas")
    public UnreadCountResponse unreadCount(Authentication authentication) {
        Long userId = authenticatedUser.id(authentication);
        return new UnreadCountResponse(messageService.countUnreadMessages(userId));
    }
}
