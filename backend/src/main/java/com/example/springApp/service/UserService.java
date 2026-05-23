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

    public User updateProfile(Long userId, String nome, String imagemUrl) {
        User user = getUserById(userId);
        user.setNome(nome);
        if (imagemUrl != null) {
            user.setImagemUrl(imagemUrl);
        }
        return userRepository.save(user);
    }

    public void deleteAccount(Long userId) {
        User user = getUserById(userId);
        userRepository.delete(user);
    }

}
