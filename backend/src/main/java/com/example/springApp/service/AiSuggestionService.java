package com.example.springApp.service;

import com.example.springApp.exception.BusinessException;
import com.example.springApp.exception.RateLimitException;
import com.example.springApp.exception.ResourceNotFoundException;
import com.example.springApp.model.WishList;
import com.example.springApp.model.WishlistItem;
import com.example.springApp.repository.WishlistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AiSuggestionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AiSuggestionService.class);
    private static final int MAX_GENERATIONS_PER_HOUR = 3;

    private final ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;
    private final WishlistRepository wishlistRepository;
    private final Clock clock;
    private final boolean aiEnabled;
    // Limite em memoria: simples para single-instance; para multiplas instancias, migrar para storage compartilhado.
    private final Map<Long, UsageWindow> usageByUser = new ConcurrentHashMap<>();

    public AiSuggestionService(
            ObjectProvider<ChatClient.Builder> chatClientBuilderProvider,
            WishlistRepository wishlistRepository,
            Clock clock,
            @Value("${app.ai.enabled:false}") boolean aiEnabled
    ) {
        this.chatClientBuilderProvider = chatClientBuilderProvider;
        this.wishlistRepository = wishlistRepository;
        this.clock = clock;
        this.aiEnabled = aiEnabled;
    }

    @Transactional
    public String generateSuggestion(WishList wishlist, Long requesterId) {
        WishList managedWishlist = wishlistRepository.findByIdWithItems(wishlist.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist nao encontrada"));
        List<WishlistItem> items = managedWishlist.getItens();
        if (items.isEmpty()) {
            throw new BusinessException("A wishlist precisa ter pelo menos um item para gerar sugestoes");
        }

        if (!aiEnabled) {
            throw new BusinessException("Sugestoes com IA estao desabilitadas neste ambiente");
        }

        consumeUsage(requesterId);

        ChatClient.Builder builder = chatClientBuilderProvider.getIfAvailable();
        if (builder == null) {
            LOGGER.error("Cliente de IA nao configurado para usuario {} e wishlist {}", requesterId, managedWishlist.getId());
            throw new BusinessException("Cliente de IA nao esta configurado");
        }

        String suggestion;
        try {
            suggestion = builder.build()
                    .prompt()
                    .system("""
                            Voce ajuda em um sistema de amigo secreto.
                            Gere sugestoes em portugues do Brasil usando exclusivamente os itens da wishlist informada.
                            Nao invente produtos fora da lista.
                            Responda em texto simples, curto, com no maximo 5 sugestoes.
                            """)
                    .user(buildPrompt(items))
                    .call()
                    .content();
        } catch (RuntimeException ex) {
            LOGGER.error("Falha ao gerar sugestao de IA para usuario {} e wishlist {}", requesterId, managedWishlist.getId(), ex);
            throw new BusinessException("Nao foi possivel gerar sugestoes com IA agora");
        }

        if (suggestion == null || suggestion.isBlank()) {
            LOGGER.error("IA retornou resposta vazia para usuario {} e wishlist {}", requesterId, managedWishlist.getId());
            throw new BusinessException("A IA nao retornou sugestoes para esta wishlist");
        }

        managedWishlist.setSugestaoIa(suggestion.trim());
        wishlistRepository.save(managedWishlist);
        return managedWishlist.getSugestaoIa();
    }

    public int remainingGenerations(Long userId) {
        UsageWindow window = usageByUser.get(userId);
        if (window == null || window.isExpired(Instant.now(clock))) {
            return MAX_GENERATIONS_PER_HOUR;
        }
        return Math.max(0, MAX_GENERATIONS_PER_HOUR - window.count());
    }

    private void consumeUsage(Long userId) {
        Instant now = Instant.now(clock);
        usageByUser.compute(userId, (id, currentWindow) -> {
            UsageWindow window = currentWindow;
            if (window == null || window.isExpired(now)) {
                window = new UsageWindow(now.plus(1, ChronoUnit.HOURS), 0);
            }
            if (window.count() >= MAX_GENERATIONS_PER_HOUR) {
                throw new RateLimitException("Limite de 3 sugestoes com IA por hora atingido");
            }
            return window.increment();
        });
    }

    private String buildPrompt(List<WishlistItem> items) {
        StringBuilder builder = new StringBuilder("Wishlist disponivel:\n");
        for (WishlistItem item : items) {
            // A IA recebe somente itens existentes para nao sugerir produtos fora da wishlist.
            builder.append("- ")
                    .append(item.getNomeProduto())
                    .append(" | link: ")
                    .append(item.getLink())
                    .append('\n');
        }
        return builder.toString();
    }

    private record UsageWindow(Instant expiresAt, int count) {

        boolean isExpired(Instant now) {
            return !expiresAt.isAfter(now);
        }

        UsageWindow increment() {
            return new UsageWindow(expiresAt, count + 1);
        }
    }
}
