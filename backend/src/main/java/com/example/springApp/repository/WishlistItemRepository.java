package com.example.springApp.repository;

import com.example.springApp.model.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    /**
     * Lista itens de wishlist pertencentes a um usuario.
     */
    List<WishlistItem> findByWishlistUsuarioId(Long usuarioId);

    /**
     * Conta itens sem carregar a lista completa.
     */
    long countByWishlistUsuarioId(Long usuarioId);

}
