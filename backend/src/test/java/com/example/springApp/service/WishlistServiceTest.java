package com.example.springApp.service;

import com.example.springApp.exception.BusinessException;
import com.example.springApp.exception.ForbiddenException;
import com.example.springApp.model.User;
import com.example.springApp.model.WishList;
import com.example.springApp.model.WishlistItem;
import com.example.springApp.repository.DrawRepository;
import com.example.springApp.repository.UserRepository;
import com.example.springApp.repository.WishlistItemRepository;
import com.example.springApp.repository.WishlistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private WishlistItemRepository wishlistItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DrawRepository drawRepository;

    @Mock
    private RealtimeNotificationService realtimeNotificationService;

    @InjectMocks
    private WishlistService wishlistService;

    @Test
    void addItemToWishlistSavesItemWhenBelowLimit() {
        User user = user(1L);
        WishList wishlist = wishlist(10L, user);
        WishlistItem item = WishlistItem.builder()
                .nomeProduto("Livro")
                .link("https://example.com/livro")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(wishlistRepository.findByUsuarioIdWithItems(1L)).thenReturn(Optional.of(wishlist));
        when(wishlistItemRepository.countByWishlistUsuarioId(1L)).thenReturn(2L);
        when(wishlistItemRepository.save(item)).thenAnswer(invocation -> {
            WishlistItem saved = invocation.getArgument(0);
            saved.setId(99L);
            return saved;
        });
        when(drawRepository.findByDestinatario_Id(1L)).thenReturn(List.of());

        WishlistItem saved = wishlistService.addItemToWishlist(1L, item);

        assertThat(saved.getId()).isEqualTo(99L);
        assertThat(saved.getWishlist()).isSameAs(wishlist);
        assertThat(wishlist.getItens()).contains(item);
        verify(wishlistItemRepository).save(item);
    }

    @Test
    void addItemToWishlistRejectsEleventhItem() {
        User user = user(1L);
        WishList wishlist = wishlist(10L, user);
        WishlistItem item = WishlistItem.builder()
                .nomeProduto("Livro")
                .link("https://example.com/livro")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(wishlistRepository.findByUsuarioIdWithItems(1L)).thenReturn(Optional.of(wishlist));
        when(wishlistItemRepository.countByWishlistUsuarioId(1L)).thenReturn(10L);

        assertThatThrownBy(() -> wishlistService.addItemToWishlist(1L, item))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("maximo 10 itens");

        verify(wishlistItemRepository, never()).save(any());
        verify(drawRepository, never()).findByDestinatario_Id(any());
    }

    @Test
    void updateItemRejectsItemFromAnotherUser() {
        User owner = user(1L);
        User anotherUser = user(2L);
        WishList wishlist = wishlist(10L, owner);
        WishlistItem existingItem = WishlistItem.builder()
                .id(50L)
                .nomeProduto("Livro")
                .link("https://example.com/livro")
                .wishlist(wishlist)
                .build();
        WishlistItem update = WishlistItem.builder()
                .nomeProduto("Livro 2")
                .link("https://example.com/livro-2")
                .build();

        when(wishlistItemRepository.findById(50L)).thenReturn(Optional.of(existingItem));

        assertThatThrownBy(() -> wishlistService.updateItem(50L, anotherUser.getId(), update))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("editar um item de outra pessoa");

        verify(wishlistItemRepository, never()).save(any());
    }

    private User user(Long id) {
        return User.builder()
                .id(id)
                .nome("User " + id)
                .email("user" + id + "@example.com")
                .build();
    }

    private WishList wishlist(Long id, User user) {
        return WishList.builder()
                .id(id)
                .usuario(user)
                .build();
    }
}
