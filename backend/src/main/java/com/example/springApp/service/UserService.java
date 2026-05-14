package com.example.springApp.service;

import com.example.springApp.exception.ResourceNotFoundException;
import com.example.springApp.repository.UserRepository;
import com.example.springApp.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
    }

}
