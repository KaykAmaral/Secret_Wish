package com.example.springApp.service;

import com.example.springApp.model.User;
import com.example.springApp.model.WishList;
import com.example.springApp.model.WishlistItem;
import com.example.springApp.repository.UserRepository;
import com.example.springApp.repository.WishlistItemRepository;
import com.example.springApp.repository.WishlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private WishlistItemRepository wishlistItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public WishList getOrCreateWishlist(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        return wishlistRepository.findByUsuarioId(userId)
                .orElseGet(() -> {
                    WishList newList = new WishList();
                    newList.setUsuario(user);
                    return wishlistRepository.save(newList);
                });
    }

    @Transactional
    public WishlistItem addItemToWishlist(Long userId, WishlistItem item) {
        WishList wishlist = getOrCreateWishlist(userId);

        item.setWishlist(wishlist);
        wishlist.getItens().add(item);

        return wishlistItemRepository.save(item);
    }

    @Transactional
    public void removeItemFromWishlist(Long itemId, Long userId) {
        WishlistItem item = wishlistItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item não encontrado."));

        // Validação de segurança: garantir que o usuário só apague itens da própria lista
        if (!item.getWishlist().getUsuario().getId().equals(userId)) {
            throw new RuntimeException("Acesso negado: você não pode deletar um item de outra pessoa.");
        }

        wishlistItemRepository.delete(item);
    }
}