package com.example.springApp.controller;

import com.example.springApp.dto.CreateGroupRequest;
import com.example.springApp.dto.GroupResponse;
import com.example.springApp.dto.JoinGroupRequest;
import com.example.springApp.mapper.ResponseMapper;
import com.example.springApp.model.Group;
import com.example.springApp.security.AuthenticatedUser;
import com.example.springApp.service.GroupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;
    private final ResponseMapper responseMapper;
    private final AuthenticatedUser authenticatedUser;

    public GroupController(
            GroupService groupService,
            ResponseMapper responseMapper,
            AuthenticatedUser authenticatedUser
    ) {
        this.groupService = groupService;
        this.responseMapper = responseMapper;
        this.authenticatedUser = authenticatedUser;
    }

    @GetMapping
    public List<GroupResponse> myGroups(Authentication authentication) {
        Long userId = authenticatedUser.id(authentication);
        return groupService.getUserGroups(userId).stream()
                .map(responseMapper::toGroupResponse)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupResponse create(
            @Valid @RequestBody CreateGroupRequest request,
            Authentication authentication
    ) {
        Long userId = authenticatedUser.id(authentication);
        Group group = Group.builder()
                .nome(request.nome())
                .dataEvento(request.dataEvento())
                .build();

        return responseMapper.toGroupResponse(groupService.createGroup(group, userId));
    }

    @PostMapping("/join")
    public GroupResponse join(
            @Valid @RequestBody JoinGroupRequest request,
            Authentication authentication
    ) {
        Long userId = authenticatedUser.id(authentication);
        return responseMapper.toGroupResponse(groupService.joinGroup(request.codigoUnico(), userId));
    }

    @DeleteMapping("/{groupId}/members/{memberId}")
    public GroupResponse removeMember(
            @PathVariable Long groupId,
            @PathVariable Long memberId,
            Authentication authentication
    ) {
        Long userId = authenticatedUser.id(authentication);
        return responseMapper.toGroupResponse(groupService.removeMember(groupId, userId, memberId));
    }

    @DeleteMapping("/{groupId}/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leave(
            @PathVariable Long groupId,
            Authentication authentication
    ) {
        Long userId = authenticatedUser.id(authentication);
        groupService.leaveGroup(groupId, userId);
    }

    @DeleteMapping("/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long groupId,
            Authentication authentication
    ) {
        Long userId = authenticatedUser.id(authentication);
        groupService.deleteGroup(groupId, userId);
    }
}
