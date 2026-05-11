package com.example.springApp.controller;

import com.example.springApp.dto.PerformDrawResponse;
import com.example.springApp.dto.SecretFriendResponse;
import com.example.springApp.mapper.ResponseMapper;
import com.example.springApp.model.Draw;
import com.example.springApp.security.AuthenticatedUser;
import com.example.springApp.service.DrawService;
import com.example.springApp.service.WishlistService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}/draw")
public class DrawController {

    private final DrawService drawService;
    private final WishlistService wishlistService;
    private final ResponseMapper responseMapper;
    private final AuthenticatedUser authenticatedUser;

    public DrawController(
            DrawService drawService,
            WishlistService wishlistService,
            ResponseMapper responseMapper,
            AuthenticatedUser authenticatedUser
    ) {
        this.drawService = drawService;
        this.wishlistService = wishlistService;
        this.responseMapper = responseMapper;
        this.authenticatedUser = authenticatedUser;
    }

    @PostMapping
    public PerformDrawResponse perform(
            @PathVariable Long groupId,
            Authentication authentication
    ) {
        Long userId = authenticatedUser.id(authentication);
        List<Draw> draws = drawService.performDraw(groupId, userId);
        return new PerformDrawResponse(
                groupId,
                draws.size(),
                draws.isEmpty() ? null : draws.getFirst().getGrupo().getDataSorteio()
        );
    }

    @GetMapping("/me")
    public SecretFriendResponse mySecretFriend(
            @PathVariable Long groupId,
            Authentication authentication
    ) {
        Long userId = authenticatedUser.id(authentication);
        Draw draw = drawService.getMeuAmigoSecreto(groupId, userId);
        return new SecretFriendResponse(
                groupId,
                responseMapper.toUserResponse(draw.getDestinatario()),
                responseMapper.toWishlistResponse(wishlistService.getVisibleWishlist(groupId, userId, draw.getDestinatario().getId()))
        );
    }
}
