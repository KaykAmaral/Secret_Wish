package com.example.springApp.controller;

import com.example.springApp.dto.NotificationResponse;
import com.example.springApp.dto.UnreadCountResponse;
import com.example.springApp.mapper.ResponseMapper;
import com.example.springApp.security.AuthenticatedUser;
import com.example.springApp.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notificacoes", description = "Consulta, leitura e limpeza de notificacoes do usuario autenticado.")
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
    @Operation(summary = "Listar notificacoes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notificacoes retornadas"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/NaoAutorizado")
    })
    public List<NotificationResponse> list(
            // Evita carregar todo o historico de notificacoes em uma unica resposta.
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            Authentication authentication
    ) {
        Long userId = authenticatedUser.id(authentication);
        return notificationService.getUserNotifications(userId, page, size).stream()
                .map(responseMapper::toNotificationResponse)
                .toList();
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Contar notificacoes nao lidas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contagem retornada"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/NaoAutorizado")
    })
    public UnreadCountResponse unreadCount(Authentication authentication) {
        Long userId = authenticatedUser.id(authentication);
        return new UnreadCountResponse(notificationService.countUnreadNotifications(userId));
    }

    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "Marcar notificacao como lida")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notificacao marcada como lida"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/NaoAutorizado"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Proibido"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NaoEncontrado")
    })
    public NotificationResponse markAsRead(
            @Parameter(description = "ID da notificacao") @PathVariable Long notificationId,
            Authentication authentication
    ) {
        Long userId = authenticatedUser.id(authentication);
        return responseMapper.toNotificationResponse(notificationService.markAsRead(notificationId, userId));
    }

    @PatchMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Marcar todas como lidas")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Notificacoes marcadas como lidas"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/NaoAutorizado")
    })
    public void markAllAsRead(Authentication authentication) {
        Long userId = authenticatedUser.id(authentication);
        notificationService.markAllAsRead(userId);
    }

    @DeleteMapping("/{notificationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir notificacao")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Notificacao excluida"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/NaoAutorizado"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Proibido"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NaoEncontrado")
    })
    public void delete(
            @Parameter(description = "ID da notificacao") @PathVariable Long notificationId,
            Authentication authentication
    ) {
        Long userId = authenticatedUser.id(authentication);
        notificationService.deleteNotification(notificationId, userId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir todas as notificacoes")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Notificacoes excluidas"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/NaoAutorizado")
    })
    public void deleteAll(Authentication authentication) {
        Long userId = authenticatedUser.id(authentication);
        notificationService.deleteUserNotifications(userId);
    }
}
