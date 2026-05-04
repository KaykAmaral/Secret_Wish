package com.example.springApp.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "wishlists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "sugestao_ia", columnDefinition = "TEXT")
    private String sugestaoIa;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User usuario;

}