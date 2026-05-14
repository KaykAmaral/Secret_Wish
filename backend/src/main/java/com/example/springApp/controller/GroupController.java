package com.example.springApp.controller;

import com.example.springApp.dto.CreateGroupRequest;
import com.example.springApp.dto.GroupResponse;
import com.example.springApp.dto.JoinGroupRequest;
import com.example.springApp.mapper.ResponseMapper;
import com.example.springApp.model.Group;
import com.example.springApp.security.AuthenticatedUser;
import com.example.springApp.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Grupos", description = "Criacao, entrada, listagem e gerenciamento de grupos de amigo secreto.")
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
    @Operation(summary = "Listar meus grupos", description = "Retorna todos os grupos que o usuario autenticado criou ou participa.")
    public List<GroupResponse> myGroups(Authentication authentication) {
        Long userId = authenticatedUser.id(authentication);
        return groupService.getUserGroups(userId).stream()
                .map(responseMapper::toGroupResponse)
                .toList();
    }

    @GetMapping("/{groupId}")
    @Operation(summary = "Consultar grupo", description = "Retorna os dados de um grupo em que o usuario autenticado participa.")
    public GroupResponse getById(
            @Parameter(description = "ID do grupo") @PathVariable Long groupId,
            Authentication authentication
    ) {
        Long userId = authenticatedUser.id(authentication);
        return responseMapper.toGroupResponse(groupService.getUserGroup(groupId, userId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar grupo", description = "Cria um novo grupo. Cada usuario pode ser dono de apenas um grupo.")
    @ApiResponse(responseCode = "201", description = "Grupo criado")
    @ApiResponse(responseCode = "400", description = "Dados invalidos ou regra de negocio violada")
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
    @Operation(summary = "Entrar em grupo", description = "Adiciona o usuario autenticado a um grupo usando o codigo unico.")
    public GroupResponse join(
            @Valid @RequestBody JoinGroupRequest request,
            Authentication authentication
    ) {
        Long userId = authenticatedUser.id(authentication);
        return responseMapper.toGroupResponse(groupService.joinGroup(request.codigoUnico(), userId));
    }

    @DeleteMapping("/{groupId}/members/{memberId}")
    @Operation(summary = "Remover membro", description = "Remove um membro do grupo. Apenas o dono pode executar esta acao.")
    public GroupResponse removeMember(
            @Parameter(description = "ID do grupo") @PathVariable Long groupId,
            @Parameter(description = "ID do membro a remover") @PathVariable Long memberId,
            Authentication authentication
    ) {
        Long userId = authenticatedUser.id(authentication);
        return responseMapper.toGroupResponse(groupService.removeMember(groupId, userId, memberId));
    }

    @DeleteMapping("/{groupId}/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Sair do grupo", description = "Permite sair do grupo antes do sorteio.")
    @ApiResponse(responseCode = "204", description = "Usuario saiu do grupo")
    public void leave(
            @Parameter(description = "ID do grupo") @PathVariable Long groupId,
            Authentication authentication
    ) {
        Long userId = authenticatedUser.id(authentication);
        groupService.leaveGroup(groupId, userId);
    }

    @DeleteMapping("/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir grupo", description = "Exclui o grupo e seus dados relacionados. Apenas o dono pode executar esta acao.")
    @ApiResponse(responseCode = "204", description = "Grupo excluido")
    public void delete(
            @Parameter(description = "ID do grupo") @PathVariable Long groupId,
            Authentication authentication
    ) {
        Long userId = authenticatedUser.id(authentication);
        groupService.deleteGroup(groupId, userId);
    }
}
