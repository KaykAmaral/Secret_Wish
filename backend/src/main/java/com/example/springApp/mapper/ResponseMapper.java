package com.example.springApp.mapper;

import com.example.springApp.dto.GroupResponse;
import com.example.springApp.dto.MessageResponse;
import com.example.springApp.dto.NotificationResponse;
import com.example.springApp.dto.UserResponse;
import com.example.springApp.dto.WishlistItemResponse;
import com.example.springApp.dto.WishlistResponse;
import com.example.springApp.model.Group;
import com.example.springApp.model.Message;
import com.example.springApp.model.Notification;
import com.example.springApp.model.User;
import com.example.springApp.model.WishList;
import com.example.springApp.model.WishlistItem;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ResponseMapper {

    public UserResponse toUserResponse(User user) {
        // Nunca expor oauthId ou outros identificadores internos do provedor.
        return new UserResponse(
                user.getId(),
                user.getNome(),
                user.getEmail()
        );
    }

    public GroupResponse toGroupResponse(Group group) {
        Set<UserResponse> members = group.getMembros().stream()
                .sorted(Comparator.comparing(User::getNome))
                .map(this::toUserResponse)
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));

        return new GroupResponse(
                group.getId(),
                group.getNome(),
                group.getCodigoUnico(),
                toUserResponse(group.getDono()),
                members,
                group.getDataCriacao(),
                group.getDataSorteio(),
                group.getDataEvento()
        );
    }

    public WishlistResponse toWishlistResponse(WishList wishlist) {
        List<WishlistItemResponse> items = wishlist.getItens().stream()
                .sorted(Comparator.comparing(WishlistItem::getId))
                .map(this::toWishlistItemResponse)
                .toList();

        return new WishlistResponse(
                wishlist.getId(),
                toUserResponse(wishlist.getUsuario()),
                wishlist.getSugestaoIa(),
                items
        );
    }

    public WishlistItemResponse toWishlistItemResponse(WishlistItem item) {
        return new WishlistItemResponse(
                item.getId(),
                item.getNomeProduto(),
                item.getLink()
        );
    }

    public MessageResponse toMessageResponse(Message message, Long viewerId) {
        boolean viewerIsSender = message.getRemetente().getId().equals(viewerId);
        // O destinatario ve apenas o apelido anonimo de quem tirou ele.
        String displayName = viewerIsSender ? message.getRemetente().getNome() : "amigo secreto";

        return new MessageResponse(
                message.getId(),
                message.getGrupo().getId(),
                viewerIsSender ? toUserResponse(message.getRemetente()) : null,
                toUserResponse(message.getDestinatario()),
                displayName,
                message.getConteudo(),
                message.getDataEnvio(),
                message.isLida(),
                message.isAnonima()
        );
    }

    public NotificationResponse toNotificationResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                toUserResponse(notification.getUsuario()),
                notification.getTitulo(),
                notification.getMensagem(),
                notification.getDataCriacao(),
                notification.isLida()
        );
    }
}
