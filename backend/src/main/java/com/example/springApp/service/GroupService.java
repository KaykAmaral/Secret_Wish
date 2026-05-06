package com.example.springApp.service;

import com.example.springApp.model.Group;
import com.example.springApp.model.User;
import com.example.springApp.repository.GroupRepository;
import com.example.springApp.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Group createGroup(Group group, Long donoId) {
        User dono = userRepository.findById(donoId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        group.setDono(dono);

        group.getMembros().add(dono);

        return groupRepository.save(group);
    }

    @Transactional
    public Group joinGroup(String codigoUnico, Long userId) {
        Group group = groupRepository.findByCodigoUnico(codigoUnico)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (group.getMembros().contains(user)) {
            throw new RuntimeException("User is already in group");
        }

        group.getMembros().add(user);

        return groupRepository.save(group);
    }
}