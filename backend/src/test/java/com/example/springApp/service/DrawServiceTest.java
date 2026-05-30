package com.example.springApp.service;

import com.example.springApp.exception.BusinessException;
import com.example.springApp.exception.ForbiddenException;
import com.example.springApp.model.Draw;
import com.example.springApp.model.Group;
import com.example.springApp.model.User;
import com.example.springApp.repository.DrawRepository;
import com.example.springApp.repository.GroupRepository;
import com.example.springApp.repository.MessageRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DrawServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private DrawRepository drawRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private DrawService drawService;

    @AfterEach
    void clearTransactionSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void performDrawRejectsNonOwnerBeforeDeletingExistingData() {
        Group group = group(user(1L, "Owner"), user(2L, "Member A"), user(3L, "Member B"));
        when(groupRepository.findById(10L)).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> drawService.performDraw(10L, 99L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Apenas o dono");

        verify(messageRepository, never()).deleteByGrupoId(any());
        verify(drawRepository, never()).deleteByGrupoId(any());
        verify(drawRepository, never()).saveAll(any());
    }

    @Test
    void performDrawRejectsGroupWithLessThanThreeMembers() {
        User owner = user(1L, "Owner");
        Group group = group(owner, user(2L, "Member A"));
        when(groupRepository.findById(10L)).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> drawService.performDraw(10L, owner.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("pelo menos 3 participantes");

        verify(messageRepository, never()).deleteByGrupoId(any());
        verify(drawRepository, never()).saveAll(any());
    }

    @Test
    void performDrawDeletesOldDataCreatesNotificationsAndSchedulesEmailAfterCommit() {
        TransactionSynchronizationManager.initSynchronization();
        User owner = user(1L, "Owner");
        User memberA = user(2L, "Member A");
        User memberB = user(3L, "Member B");
        Group group = group(owner, memberA, memberB);
        List<EmailService.DrawResultEmail> emailResults = List.of(
                new EmailService.DrawResultEmail(10L, "Grupo", 1L, "Owner", "owner@example.com", "Member A")
        );

        when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
        when(groupRepository.save(group)).thenReturn(group);
        when(drawRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(emailService.toDrawResultEmails(any())).thenReturn(emailResults);

        List<Draw> draws = drawService.performDraw(10L, owner.getId());

        assertThat(draws).hasSize(3);
        assertThat(draws).allSatisfy(draw -> {
            assertThat(draw.getGrupo()).isSameAs(group);
            assertThat(draw.getRemetente()).isNotEqualTo(draw.getDestinatario());
        });
        assertThat(group.getDataSorteio()).isNotNull();
        verify(messageRepository).deleteByGrupoId(10L);
        verify(drawRepository).deleteByGrupoId(10L);
        verify(drawRepository).flush();
        verify(groupRepository).save(group);
        verify(notificationService, times(3)).createNotification(any(), any(), any());

        ArgumentCaptor<Iterable<Draw>> drawCaptor = ArgumentCaptor.forClass(Iterable.class);
        verify(drawRepository).saveAll(drawCaptor.capture());
        assertThat(drawCaptor.getValue()).hasSize(3);

        assertThat(TransactionSynchronizationManager.getSynchronizations()).hasSize(1);
        TransactionSynchronization synchronization = TransactionSynchronizationManager.getSynchronizations().getFirst();
        synchronization.afterCommit();
        verify(emailService).sendDrawResults(emailResults);
    }

    private Group group(User... members) {
        Group group = Group.builder()
                .id(10L)
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
                .email(name.toLowerCase().replace(" ", ".") + "@example.com")
                .build();
    }
}
