package com.example.springApp.controller;

import com.example.springApp.dto.UserResponse;
import com.example.springApp.mapper.ResponseMapper;
import com.example.springApp.security.AuthenticatedUser;
import com.example.springApp.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
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
    public UserResponse me(Authentication authentication) {
        Long userId = authenticatedUser.id(authentication);
        return responseMapper.toUserResponse(userService.getUserById(userId));
    }
}
