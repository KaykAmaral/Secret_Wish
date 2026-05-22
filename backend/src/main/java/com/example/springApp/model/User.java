package com.example.springApp.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String imagemUrl;

    @Column(nullable = false, unique = true)
    private String email;

    private String password;

    @Column(name = "auth_id", unique = true)
    private String oauthId;

}