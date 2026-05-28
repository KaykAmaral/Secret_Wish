package com.example.springApp.service;

import com.example.springApp.exception.BusinessException;
import com.example.springApp.exception.RateLimitException;
import com.example.springApp.model.WishList;
import com.example.springApp.model.WishlistItem;
import com.example.springApp.repository.WishlistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiSuggestionServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-05-27T12:00:00Z"),
            ZoneId.of("UTC")
    );

    @Mock
    private ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;

    @Mock
    private WishlistRepository wishlistRepository;

    @Test
    void generateSuggestionRejectsEmptyWishlistBeforeConsumingUsage() {
        // Validacoes baratas devem acontecer antes de consumir cota ou chamar o cliente de IA.
        AiSuggestionService service = new AiSuggestionService(
                chatClientBuilderProvider,
                wishlistRepository,
                FIXED_CLOCK,
                true
        );
        WishList wishlist = WishList.builder().id(10L).build();

        when(wishlistRepository.findByIdWithItems(10L)).thenReturn(Optional.of(wishlist));

        assertThatThrownBy(() -> service.generateSuggestion(wishlist, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("pelo menos um item");
        assertThat(service.remainingGenerations(1L)).isEqualTo(3);
        verify(chatClientBuilderProvider, never()).getIfAvailable();
    }

    @Test
    void generateSuggestionRejectsDisabledAiWithoutConsumingUsage() {
        AiSuggestionService service = new AiSuggestionService(
                chatClientBuilderProvider,
                wishlistRepository,
                FIXED_CLOCK,
                false
        );
        WishList wishlist = wishlistWithItem(10L);

        when(wishlistRepository.findByIdWithItems(10L)).thenReturn(Optional.of(wishlist));

        assertThatThrownBy(() -> service.generateSuggestion(wishlist, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("desabilitadas");
        assertThat(service.remainingGenerations(1L)).isEqualTo(3);
        verify(chatClientBuilderProvider, never()).getIfAvailable();
    }

    @Test
    void generateSuggestionConsumesUsageAndThenRateLimitsUser() {
        // Mesmo quando a IA esta indisponivel, a tentativa conta para evitar retry agressivo.
        AiSuggestionService service = new AiSuggestionService(
                chatClientBuilderProvider,
                wishlistRepository,
                FIXED_CLOCK,
                true
        );
        WishList wishlist = wishlistWithItem(10L);

        when(wishlistRepository.findByIdWithItems(10L)).thenReturn(Optional.of(wishlist));
        when(chatClientBuilderProvider.getIfAvailable()).thenReturn(null);

        for (int attempt = 0; attempt < 3; attempt++) {
            assertThatThrownBy(() -> service.generateSuggestion(wishlist, 1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Cliente de IA");
        }

        assertThat(service.remainingGenerations(1L)).isZero();
        assertThatThrownBy(() -> service.generateSuggestion(wishlist, 1L))
                .isInstanceOf(RateLimitException.class)
                .hasMessageContaining("Limite de 3 sugestoes");
    }

    private WishList wishlistWithItem(Long id) {
        WishList wishlist = WishList.builder().id(id).build();
        wishlist.getItens().add(WishlistItem.builder()
                .nomeProduto("Livro")
                .link("https://example.com/livro")
                .build());
        return wishlist;
    }
}
