package com.example.springApp.service;

import com.example.springApp.exception.ResourceNotFoundException;
import com.example.springApp.model.User;
import com.example.springApp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void getUserByIdAndEmailTranslateMissingUserToDomainError() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuario nao encontrado");
        assertThatThrownBy(() -> userService.getUserByEmail("missing@example.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuario nao encontrado");
    }

    @Test
    void updateProfileChangesEditableFieldsAndPreservesImageWhenNull() {
        User user = User.builder()
                .id(1L)
                .nome("Antigo")
                .email("user@example.com")
                .imagemUrl("https://example.com/old.png")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User updated = userService.updateProfile(1L, "Novo", null);

        assertThat(updated.getNome()).isEqualTo("Novo");
        assertThat(updated.getImagemUrl()).isEqualTo("https://example.com/old.png");
        verify(userRepository).save(user);
    }

    @Test
    void deleteAccountDeletesLoadedUser() {
        User user = User.builder().id(1L).email("user@example.com").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteAccount(1L);

        verify(userRepository).delete(user);
    }
}
