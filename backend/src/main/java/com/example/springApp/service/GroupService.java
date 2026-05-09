package com.example.springApp.service;

import com.example.springApp.exception.ConflictException;
import com.example.springApp.exception.BusinessException;
import com.example.springApp.exception.ForbiddenException;
import com.example.springApp.exception.ResourceNotFoundException;
import com.example.springApp.model.Group;
import com.example.springApp.model.User;
import com.example.springApp.repository.DrawRepository;
import com.example.springApp.repository.GroupRepository;
import com.example.springApp.repository.MessageRepository;
import com.example.springApp.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class GroupService {

    private static final String CODE_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DrawRepository drawRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Transactional
    public Group createGroup(Group group, Long donoId) {
        User dono = userRepository.findById(donoId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));

        if (groupRepository.existsByDonoId(donoId)) {
            throw new ConflictException("Usuario ja possui um grupo criado");
        }

        group.setDono(dono);
        group.setCodigoUnico(generateUniqueCode());
        group.getMembros().add(dono);

        return groupRepository.save(group);
    }

    @Transactional
    public Group joinGroup(String codigoUnico, Long userId) {
        Group group = groupRepository.findByCodigoUnico(codigoUnico)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo nao encontrado"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));

        if (group.getMembros().contains(user)) {
            throw new ConflictException("Usuario ja esta no grupo");
        }

        group.getMembros().add(user);

        return groupRepository.save(group);
    }

    @Transactional
    public Group removeMember(Long groupId, Long donoId, Long memberId) {
        Group group = getGroupForOwner(groupId, donoId);

        if (drawRepository.existsByGrupoId(groupId)) {
            throw new BusinessException("Participantes nao podem ser removidos depois do sorteio");
        }

        if (group.getDono().getId().equals(memberId)) {
            throw new BusinessException("O dono nao pode ser removido do proprio grupo");
        }

        boolean removed = group.getMembros().removeIf(user -> user.getId().equals(memberId));
        if (!removed) {
            throw new ResourceNotFoundException("Participante nao encontrado no grupo");
        }

        return groupRepository.save(group);
    }

    @Transactional
    public void leaveGroup(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo nao encontrado"));

        if (drawRepository.existsByGrupoId(groupId)) {
            throw new BusinessException("Participantes nao podem sair depois do sorteio");
        }

        if (group.getDono().getId().equals(userId)) {
            throw new BusinessException("O dono deve deletar o grupo em vez de sair dele");
        }

        boolean removed = group.getMembros().removeIf(user -> user.getId().equals(userId));
        if (!removed) {
            throw new ResourceNotFoundException("Usuario nao participa deste grupo");
        }

        groupRepository.save(group);
    }

    @Transactional
    public void deleteGroup(Long groupId, Long donoId) {
        Group group = getGroupForOwner(groupId, donoId);
        messageRepository.deleteByGrupoId(groupId);
        drawRepository.deleteByGrupoId(groupId);
        groupRepository.delete(group);
    }

    private Group getGroupForOwner(Long groupId, Long donoId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo nao encontrado"));

        if (!group.getDono().getId().equals(donoId)) {
            throw new ForbiddenException("Apenas o dono do grupo pode executar esta acao");
        }

        return group;
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = generateCode();
        } while (groupRepository.findByCodigoUnico(code).isPresent());
        return code;
    }

    private String generateCode() {
        StringBuilder builder = new StringBuilder(9);
        for (int i = 0; i < 8; i++) {
            if (i == 4) {
                builder.append('-');
            }
            builder.append(CODE_ALPHABET.charAt(RANDOM.nextInt(CODE_ALPHABET.length())));
        }
        return builder.toString();
    }

}
