package com.example.springApp.repository;

import com.example.springApp.model.WishList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<WishList, Long> {

    Optional<WishList> findByUsuarioId(Long userId);

    @Query("select wishlist from WishList wishlist left join fetch wishlist.itens where wishlist.usuario.id = :userId")
    Optional<WishList> findByUsuarioIdWithItems(Long userId);

    @Query("select wishlist from WishList wishlist left join fetch wishlist.itens where wishlist.id = :id")
    Optional<WishList> findByIdWithItems(Long id);

}
