package com.example.springApp.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "wishlist_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_produto", nullable = false)
    private String nomeProduto;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String link;

    @ManyToOne
    @JoinColumn(name = "wishlist_id", nullable = false)
    private WishList wishlist;

}
