package com.example.springApp.service;

import com.example.springApp.exception.ConflictException;
import com.example.springApp.exception.ResourceNotFoundException;
import com.example.springApp.model.Group;
import com.example.springApp.model.User;
import com.example.springApp.repository.GroupRepository;
import com.example.springApp.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Group createGroup(Group group, Long donoId) {
        User dono = userRepository.findById(donoId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));

        if (groupRepository.existsByDonoId(donoId)) {
            throw new ConflictException("Usuario ja possui um grupo criado");
        }

        group.setDono(dono);
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

}
