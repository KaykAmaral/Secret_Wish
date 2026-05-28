package com.example.springApp.service;

import com.example.springApp.exception.BusinessException;
import com.example.springApp.exception.ConflictException;
import com.example.springApp.exception.ForbiddenException;
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

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-05-27T12:00:00Z"),
            ZoneId.of("UTC")
    );

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DrawRepository drawRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private Clock clock;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private GroupService groupService;

    @Test
    void createGroupAddsOwnerAsMemberAndGeneratesCode() {
        setupClock();
        User owner = user(1L);
        Group group = Group.builder()
                .nome("Grupo")
                .descricao("Descricao")
                .dataEvento(LocalDateTime.of(2026, 12, 24, 20, 0))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(groupRepository.existsByDonoId(1L)).thenReturn(false);
        when(groupRepository.findByCodigoUnico(any())).thenReturn(Optional.empty());
        when(groupRepository.save(group)).thenAnswer(invocation -> invocation.getArgument(0));

        Group saved = groupService.createGroup(group, 1L);

        assertThat(saved.getDono()).isSameAs(owner);
        assertThat(saved.getMembros()).containsExactly(owner);
        assertThat(saved.getCodigoUnico()).matches("^[A-Z0-9]{4}-[A-Z0-9]{4}$");
        verify(groupRepository).save(group);
    }

    @Test
    void createGroupRejectsSecondGroupForSameOwner() {
        setupClock();
        User owner = user(1L);
        Group group = Group.builder()
                .nome("Grupo")
                .descricao("Descricao")
                .dataEvento(LocalDateTime.of(2026, 12, 24, 20, 0))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(groupRepository.existsByDonoId(1L)).thenReturn(true);

        assertThatThrownBy(() -> groupService.createGroup(group, 1L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("ja possui um grupo");

        verify(groupRepository, never()).save(any());
    }

    @Test
    void createGroupRejectsEventDateOutsideAllowedWindow() {
        setupClock();
        User owner = user(1L);
        Group group = Group.builder()
                .nome("Grupo")
                .descricao("Descricao")
                .dataEvento(LocalDateTime.of(2028, 6, 1, 20, 0))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        assertThatThrownBy(() -> groupService.createGroup(group, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("24 meses");

        verify(groupRepository, never()).existsByDonoId(any());
        verify(groupRepository, never()).save(any());
    }

    @Test
    void removeMemberRejectsNonOwner() {
        User owner = user(1L);
        User member = user(2L);
        Group group = Group.builder()
                .id(10L)
                .nome("Grupo")
                .dono(owner)
                .build();
        group.getMembros().add(owner);
        group.getMembros().add(member);

        when(groupRepository.findById(10L)).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> groupService.removeMember(10L, member.getId(), owner.getId()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Apenas o dono");

        verify(groupRepository, never()).save(any());
    }

    private User user(Long id) {
        return User.builder()
                .id(id)
                .nome("User " + id)
                .email("user" + id + "@example.com")
                .build();
    }

    private void setupClock() {
        when(clock.instant()).thenReturn(FIXED_CLOCK.instant());
        when(clock.getZone()).thenReturn(FIXED_CLOCK.getZone());
    }
}
