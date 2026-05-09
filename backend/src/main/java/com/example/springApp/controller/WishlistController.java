package com.example.springApp.controller;

import com.example.springApp.dto.WishlistItemRequest;
import com.example.springApp.dto.WishlistItemResponse;
import com.example.springApp.dto.WishlistResponse;
import com.example.springApp.mapper.ResponseMapper;
import com.example.springApp.model.WishlistItem;
import com.example.springApp.security.AuthenticatedUser;
import com.example.springApp.service.WishlistService;
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
public class WishlistController {

    private final WishlistService wishlistService;
    private final ResponseMapper responseMapper;
    private final AuthenticatedUser authenticatedUser;

    public WishlistController(
            WishlistService wishlistService,
            ResponseMapper responseMapper,
            AuthenticatedUser authenticatedUser
    ) {
        this.wishlistService = wishlistService;
        this.responseMapper = responseMapper;
        this.authenticatedUser = authenticatedUser;
    }

    @GetMapping("/wishlist")
    public WishlistResponse myWishlist(Authentication authentication) {
        Long userId = authenticatedUser.id(authentication);
        return responseMapper.toWishlistResponse(wishlistService.getOrCreateWishlist(userId));
    }

    @PostMapping("/wishlist/items")
    @ResponseStatus(HttpStatus.CREATED)
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
    public WishlistItemResponse updateItem(
            @PathVariable Long itemId,
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
    public void removeItem(
            @PathVariable Long itemId,
            Authentication authentication
    ) {
        Long userId = authenticatedUser.id(authentication);
        wishlistService.removeItemFromWishlist(itemId, userId);
    }

    @GetMapping("/groups/{groupId}/users/{ownerId}/wishlist")
    public WishlistResponse visibleWishlist(
            @PathVariable Long groupId,
            @PathVariable Long ownerId,
            Authentication authentication
    ) {
        Long userId = authenticatedUser.id(authentication);
        return responseMapper.toWishlistResponse(wishlistService.getVisibleWishlist(groupId, userId, ownerId));
    }
}
