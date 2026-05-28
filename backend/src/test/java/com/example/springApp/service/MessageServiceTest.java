package com.example.springApp.service;

import com.example.springApp.dto.ChatSummaryResponse;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        when(drawRepository.existsByGrupo_IdAndRemetente_IdAndDestinatario_Id(10L, 1L, 2L)).thenReturn(false);
        when(drawRepository.existsByGrupo_IdAndRemetente_IdAndDestinatario_Id(10L, 2L, 1L)).thenReturn(false);

        assertThatThrownBy(() -> messageService.getConversation(10L, 1L, 2L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Voce nao pode acessar esta conversa");
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

        when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
        when(drawRepository.findUserDrawRelations(10L, 1L)).thenReturn(List.of(draw));
        when(messageRepository.countByGrupoIdAndRemetenteIdAndDestinatarioIdAndLidaFalse(10L, 2L, 1L))
                .thenReturn(3L);

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

        when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
        when(drawRepository.findUserDrawRelations(10L, 2L)).thenReturn(List.of(draw));
        when(messageRepository.countByGrupoIdAndRemetenteIdAndDestinatarioIdAndLidaFalse(10L, 1L, 2L))
                .thenReturn(1L);

        List<ChatSummaryResponse> summaries = messageService.getChatSummaries(10L, 2L);

        assertThat(summaries).containsExactly(new ChatSummaryResponse(10L, 1L, "amigo secreto", true, 1L));
    }

    @Test
    void getChatSummariesRejectsUserOutsideGroup() {
        User member = user(1L, "Ana");
        Group group = group(10L, member);

        when(groupRepository.findById(10L)).thenReturn(Optional.of(group));

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
}
