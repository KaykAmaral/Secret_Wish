package com.example.springApp.service;

import com.example.springApp.exception.ForbiddenException;
import com.example.springApp.exception.ResourceNotFoundException;
import com.example.springApp.model.User;
import com.example.springApp.model.WishList;
import com.example.springApp.model.WishlistItem;
import com.example.springApp.repository.DrawRepository;
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

    @Autowired
    private DrawRepository drawRepository;

    @Autowired
    private RealtimeNotificationService realtimeNotificationService;

    @Transactional
    public WishList getOrCreateWishlist(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));

        return wishlistRepository.findByUsuarioIdWithItems(userId)
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

        WishlistItem saved = wishlistItemRepository.save(item);
        notifyGivers(userId);
        return saved;
    }

    @Transactional
    public WishlistItem updateItem(Long itemId, Long userId, WishlistItem updatedItem) {
        WishlistItem item = wishlistItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item nao encontrado"));

        if (!item.getWishlist().getUsuario().getId().equals(userId)) {
            throw new ForbiddenException("Voce nao pode editar um item de outra pessoa");
        }

        item.setNomeProduto(updatedItem.getNomeProduto());
        item.setLink(updatedItem.getLink());
        WishlistItem saved = wishlistItemRepository.save(item);
        notifyGivers(userId);
        return saved;
    }

    @Transactional
    public WishList getVisibleWishlist(Long groupId, Long viewerId, Long ownerId) {
        if (viewerId.equals(ownerId)) {
            return getOrCreateWishlist(ownerId);
        }

        // A wishlist de outra pessoa so fica visivel para quem tirou essa pessoa.
        boolean canView = drawRepository.existsByGrupo_IdAndRemetente_IdAndDestinatario_Id(
                groupId,
                viewerId,
                ownerId
        );

        if (!canView) {
            throw new ForbiddenException("Voce nao pode visualizar esta wishlist");
        }

        return getOrCreateWishlist(ownerId);
    }

    @Transactional
    public void removeItemFromWishlist(Long itemId, Long userId) {
        WishlistItem item = wishlistItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item nao encontrado"));

        if (!item.getWishlist().getUsuario().getId().equals(userId)) {
            throw new ForbiddenException("Voce nao pode deletar um item de outra pessoa");
        }

        wishlistItemRepository.delete(item);
        notifyGivers(userId);
    }

    private void notifyGivers(Long userId) {
        drawRepository.findByDestinatario_Id(userId).forEach(draw -> {
            realtimeNotificationService.notifyWishlistUpdate(draw.getRemetente().getId(), draw.getGrupo().getId());
        });
    }

}
