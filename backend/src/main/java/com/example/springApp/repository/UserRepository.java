package com.example.springApp.repository;

import com.example.springApp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca usuario local pelo email usado em login e cadastro.
     */
    Optional<User> findByEmail(String email);

    /**
     * Busca usuario vinculado a uma conta Google.
     */
    Optional<User> findByOauthId(String oauthId);

    /**
     * Verifica duplicidade antes de criar conta por email.
     */
    boolean existsByEmail(String email);

}
