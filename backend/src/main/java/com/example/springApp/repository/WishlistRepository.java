package com.example.springApp.repository;

import com.example.springApp.model.WishList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<WishList, Long> {

    Optional<WishList> findByUsuarioId(Long userId);

}