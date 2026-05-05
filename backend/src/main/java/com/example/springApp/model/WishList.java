package com.example.springApp.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "sugestao_ia", columnDefinition = "TEXT")
    private String sugestaoIa;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User usuario;

    @OneToMany(mappedBy = "wishlist", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WishlistItem> itens = new ArrayList<>();

}
