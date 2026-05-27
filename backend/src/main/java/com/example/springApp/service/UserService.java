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

    /**
     * Busca usuario por id e converte ausencia em erro de dominio.
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
    }

    /**
     * Busca usuario pelo email usado nos fluxos de autenticacao.
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
    }

    /**
     * Atualiza campos editaveis do perfil sem permitir troca de email por esse fluxo.
     */
    public User updateProfile(Long userId, String nome, String imagemUrl) {
        User user = getUserById(userId);
        user.setNome(nome);
        if (imagemUrl != null) {
            user.setImagemUrl(imagemUrl);
        }
        return userRepository.save(user);
    }

    /**
     * Remove a conta do usuario autenticado.
     */
    public void deleteAccount(Long userId) {
        User user = getUserById(userId);
        userRepository.delete(user);
    }

}
