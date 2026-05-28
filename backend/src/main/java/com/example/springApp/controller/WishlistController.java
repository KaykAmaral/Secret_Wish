package com.example.springApp.controller;

import com.example.springApp.dto.AiSuggestionResponse;
import com.example.springApp.dto.WishlistItemRequest;
import com.example.springApp.dto.WishlistItemResponse;
import com.example.springApp.dto.WishlistResponse;
import com.example.springApp.mapper.ResponseMapper;
import com.example.springApp.model.WishList;
import com.example.springApp.model.WishlistItem;
import com.example.springApp.security.AuthenticatedUser;
import com.example.springApp.service.AiSuggestionService;
import com.example.springApp.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Wishlists", description = "Gerenciamento da propria wishlist e visualizacao permitida apos o sorteio.")
public class WishlistController {

    private final WishlistService wishlistService;
    private final AiSuggestionService aiSuggestionService;
    private final ResponseMapper responseMapper;
    private final AuthenticatedUser authenticatedUser;

    public WishlistController(
            WishlistService wishlistService,
            AiSuggestionService aiSuggestionService,
            ResponseMapper responseMapper,
            AuthenticatedUser authenticatedUser
    ) {
        this.wishlistService = wishlistService;
        this.aiSuggestionService = aiSuggestionService;
        this.responseMapper = responseMapper;
        this.authenticatedUser = authenticatedUser;
    }

    @GetMapping("/wishlist")
    @Operation(summary = "Consultar minha wishlist", description = "Retorna a wishlist do usuario autenticado, criando uma vazia se ainda nao existir.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Wishlist retornada"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/NaoAutorizado")
    })
    public WishlistResponse myWishlist(Authentication authentication) {
        Long userId = authenticatedUser.id(authentication);
        return responseMapper.toWishlistResponse(wishlistService.getOrCreateWishlist(userId));
    }

    @PostMapping("/wishlist/items")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Adicionar item na wishlist", description = "Adiciona um item simples com nome e link.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item criado"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/ErroPadrao"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/NaoAutorizado")
    })
    public WishlistItemResponse addItem(
            @Valid @RequestBody WishlistItemRequest request,
            Authentication authentication
    ) {
        Long userId = authenticatedUser.id(authentication);
        WishlistItem item = WishlistItem.builder()
                .nomeProduto(request.nomeProduto())
                .link(request.link())
                .build();
        return responseMapper.toWishlistItemResponse(wishlistService.addItemToWishlist(userId, item));
    }

    @PutMapping("/wishlist/items/{itemId}")
    @Operation(summary = "Atualizar item da wishlist", description = "Atualiza nome e link de um item da wishlist do usuario autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item atualizado"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/ErroPadrao"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/NaoAutorizado"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Proibido"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NaoEncontrado")
    })
    public WishlistItemResponse updateItem(
            @Parameter(description = "ID do item") @PathVariable Long itemId,
            @Valid @RequestBody WishlistItemRequest request,
            Authentication authentication
    ) {
        Long userId = authenticatedUser.id(authentication);
        WishlistItem item = WishlistItem.builder()
                .nomeProduto(request.nomeProduto())
                .link(request.link())
                .build();
        return responseMapper.toWishlistItemResponse(wishlistService.updateItem(itemId, userId, item));
    }

    @DeleteMapping("/wishlist/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remover item da wishlist")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Item removido"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/NaoAutorizado"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Proibido"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NaoEncontrado")
    })
    public void removeItem(
            @Parameter(description = "ID do item") @PathVariable Long itemId,
            Authentication authentication
    ) {
        Long userId = authenticatedUser.id(authentication);
        wishlistService.removeItemFromWishlist(itemId, userId);
    }

    @GetMapping("/groups/{groupId}/users/{ownerId}/wishlist")
    @Operation(
            summary = "Consultar wishlist visivel",
            description = "Retorna a wishlist de outro usuario apenas quando a regra do sorteio permitir a visualizacao."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Wishlist visivel retornada"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/NaoAutorizado"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Proibido")
    })
    public WishlistResponse visibleWishlist(
            @Parameter(description = "ID do grupo") @PathVariable Long groupId,
            @Parameter(description = "ID do usuario dono da wishlist") @PathVariable Long ownerId,
            Authentication authentication
    ) {
        Long userId = authenticatedUser.id(authentication);
        return responseMapper.toWishlistResponse(wishlistService.getVisibleWishlist(groupId, userId, ownerId));
    }

    @PostMapping("/groups/{groupId}/users/{ownerId}/wishlist/ai-suggestion")
    @Operation(
            summary = "Gerar sugestao por IA",
            description = "Gera uma sugestao textual baseada apenas nos itens presentes na wishlist visivel."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sugestao gerada"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/ErroPadrao"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/NaoAutorizado"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Proibido"),
            @ApiResponse(responseCode = "429", description = "Limite de uso por hora atingido")
    })
    public AiSuggestionResponse generateAiSuggestion(
            @Parameter(description = "ID do grupo") @PathVariable Long groupId,
            @Parameter(description = "ID do usuario dono da wishlist") @PathVariable Long ownerId,
            Authentication authentication
    ) {
        Long userId = authenticatedUser.id(authentication);
        WishList wishlist = wishlistService.getVisibleWishlist(groupId, userId, ownerId);
        String suggestion = aiSuggestionService.generateSuggestion(wishlist, userId);
        return new AiSuggestionResponse(wishlist.getId(), suggestion);
    }
}
