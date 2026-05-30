package com.example.springApp.service;

import com.example.springApp.dto.ChatSummaryResponse;
import com.example.springApp.dto.UnreadConversationCount;
import com.example.springApp.exception.ForbiddenException;
import com.example.springApp.model.Draw;
import com.example.springApp.model.Group;
import com.example.springApp.model.User;
import com.example.springApp.repository.DrawRepository;
import com.example.springApp.repository.GroupRepository;
import com.example.springApp.repository.MessageRepository;
import com.example.springApp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DrawRepository drawRepository;

    @Mock
    private RealtimeNotificationService realtimeNotificationService;

    @InjectMocks
    private MessageService messageService;

    @Test
    void getConversationRejectsUsersOutsideDrawPair() {
        when(drawRepository.existsDirectPairInEitherDirection(10L, 1L, 2L)).thenReturn(false);

        assertThatThrownBy(() -> messageService.getConversation(10L, 1L, 2L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Voce nao pode acessar esta conversa");
    }

    @Test
    void getConversationUsesDefaultLimitWhenRequestedSizeIsInvalid() {
        when(drawRepository.existsDirectPairInEitherDirection(10L, 1L, 2L)).thenReturn(true);
        when(messageRepository.findConversation(eq(10L), eq(1L), eq(2L), any(Pageable.class)))
                .thenReturn(List.of());

        messageService.getConversation(10L, 1L, 2L, -5, 0);

        verify(messageRepository).findConversation(eq(10L), eq(1L), eq(2L), org.mockito.ArgumentMatchers.argThat(pageable ->
                pageable.getPageNumber() == 0 && pageable.getPageSize() == 50
        ));
    }

    @Test
    void getConversationCapsRequestedSizeAtMaximumLimit() {
        when(drawRepository.existsDirectPairInEitherDirection(10L, 1L, 2L)).thenReturn(true);
        when(messageRepository.findConversation(eq(10L), eq(1L), eq(2L), any(Pageable.class)))
                .thenReturn(List.of());

        messageService.getConversation(10L, 1L, 2L, 2, 500);

        verify(messageRepository).findConversation(eq(10L), eq(1L), eq(2L), org.mockito.ArgumentMatchers.argThat(pageable ->
                pageable.getPageNumber() == 2 && pageable.getPageSize() == 100
        ));
    }

    @Test
    void getChatSummariesShowsReceiverNameWhenUserIsGiver() {
        User giver = user(1L, "Ana");
        User receiver = user(2L, "Bruno");
        Group group = group(10L, giver, receiver);
        Draw draw = Draw.builder()
                .grupo(group)
                .remetente(giver)
                .destinatario(receiver)
                .build();

        when(groupRepository.existsByIdAndMembros_Id(10L, 1L)).thenReturn(true);
        when(drawRepository.findUserDrawRelations(10L, 1L)).thenReturn(List.of(draw));
        // O teste valida a otimizacao: contadores de nao lidas chegam em lote, nao uma query por chat.
        when(messageRepository.countUnreadByConversationPartners(10L, 1L, List.of(2L)))
                .thenReturn(List.of(unreadCount(2L, 3L)));

        List<ChatSummaryResponse> summaries = messageService.getChatSummaries(10L, 1L);

        assertThat(summaries).containsExactly(new ChatSummaryResponse(10L, 2L, "Bruno", false, 3L));
    }

    @Test
    void getChatSummariesHidesGiverNameWhenUserIsReceiver() {
        User giver = user(1L, "Ana");
        User receiver = user(2L, "Bruno");
        Group group = group(10L, giver, receiver);
        Draw draw = Draw.builder()
                .grupo(group)
                .remetente(giver)
                .destinatario(receiver)
                .build();

        when(groupRepository.existsByIdAndMembros_Id(10L, 2L)).thenReturn(true);
        when(drawRepository.findUserDrawRelations(10L, 2L)).thenReturn(List.of(draw));
        when(messageRepository.countUnreadByConversationPartners(10L, 2L, List.of(1L)))
                .thenReturn(List.of(unreadCount(1L, 1L)));

        List<ChatSummaryResponse> summaries = messageService.getChatSummaries(10L, 2L);

        assertThat(summaries).containsExactly(new ChatSummaryResponse(10L, 1L, "amigo secreto", true, 1L));
    }

    @Test
    void getChatSummariesRejectsUserOutsideGroup() {
        when(groupRepository.existsByIdAndMembros_Id(10L, 99L)).thenReturn(false);
        when(groupRepository.existsById(10L)).thenReturn(true);

        assertThatThrownBy(() -> messageService.getChatSummaries(10L, 99L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Voce nao faz parte deste grupo");
    }

    private Group group(Long id, User... members) {
        Group group = Group.builder()
                .id(id)
                .nome("Grupo")
                .dono(members[0])
                .build();
        group.getMembros().addAll(List.of(members));
        return group;
    }

    private User user(Long id, String name) {
        return User.builder()
                .id(id)
                .nome(name)
                .email("user" + id + "@example.com")
                .build();
    }

    private UnreadConversationCount unreadCount(Long otherUserId, Long unreadCount) {
        // Projecao simples equivalente ao retorno parcial do Spring Data na query agregada.
        return new UnreadConversationCount() {
            @Override
            public Long getOtherUserId() {
                return otherUserId;
            }

            @Override
            public Long getUnreadCount() {
                return unreadCount;
            }
        };
    }
}
