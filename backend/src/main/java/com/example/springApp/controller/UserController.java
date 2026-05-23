package com.example.springApp.controller;

import com.example.springApp.dto.UpdateProfileRequest;
import com.example.springApp.dto.UserResponse;
import com.example.springApp.mapper.ResponseMapper;
import com.example.springApp.security.AuthenticatedUser;
import com.example.springApp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name = "Usuarios", description = "Gerenciamento de perfil e dados do usuario.")
public class UserController {

    private final UserService userService;
    private final ResponseMapper responseMapper;
    private final AuthenticatedUser authenticatedUser;

    public UserController(
            UserService userService,
            ResponseMapper responseMapper,
            AuthenticatedUser authenticatedUser
    ) {
        this.userService = userService;
        this.responseMapper = responseMapper;
        this.authenticatedUser = authenticatedUser;
    }

    @GetMapping("/me")
    @Operation(summary = "Consultar usuario autenticado")
    public UserResponse me(Authentication authentication) {
        Long userId = authenticatedUser.id(authentication);
        return responseMapper.toUserResponse(userService.getUserById(userId));
    }

    @PutMapping("/me")
    @Operation(summary = "Atualizar perfil do usuario")
    public UserResponse updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication
    ) {
        Long userId = authenticatedUser.id(authentication);
        return responseMapper.toUserResponse(userService.updateProfile(userId, request.nome(), request.imagemUrl()));
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir conta do usuario")
    public void deleteAccount(Authentication authentication) {
        Long userId = authenticatedUser.id(authentication);
        userService.deleteAccount(userId);
    }
}
