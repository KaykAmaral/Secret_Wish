package com.example.springApp.repository;

import com.example.springApp.model.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    List<WishlistItem> findByWishlistUsuarioId(Long usuarioId);
    long countByWishlistUsuarioId(Long usuarioId);

}
