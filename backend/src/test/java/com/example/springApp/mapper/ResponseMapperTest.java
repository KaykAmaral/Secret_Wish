package com.example.springApp.mapper;

import com.example.springApp.dto.GroupResponse;
import com.example.springApp.dto.MessageResponse;
import com.example.springApp.dto.WishlistResponse;
import com.example.springApp.model.Group;
import com.example.springApp.model.Message;
import com.example.springApp.model.User;
import com.example.springApp.model.WishList;
import com.example.springApp.model.WishlistItem;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResponseMapperTest {

    private final ResponseMapper mapper = new ResponseMapper();

    @Test
    void groupResponseSortsMembersAndSummaryKeepsOnlyCount() {
        User owner = user(1L, "Carla");
        User bruno = user(2L, "Bruno");
        User ana = user(3L, "Ana");
        Group group = Group.builder()
                .id(10L)
                .nome("Grupo")
                .descricao("Descricao")
                .codigoUnico("ABCD-1234")
                .dono(owner)
                .build();
        group.getMembros().addAll(List.of(owner, bruno, ana));

        GroupResponse full = mapper.toGroupResponse(group);
        GroupResponse summary = mapper.toGroupSummaryResponse(group, 3);

        assertThat(full.membros()).extracting("nome").containsExactly("Ana", "Bruno", "Carla");
        assertThat(full.totalMembros()).isEqualTo(3);
        assertThat(summary.membros()).isEmpty();
        assertThat(summary.totalMembros()).isEqualTo(3);
    }

    @Test
    void wishlistResponseSortsItemsById() {
        User user = user(1L, "Ana");
        WishList wishlist = WishList.builder()
                .id(20L)
                .usuario(user)
                .sugestaoIa("Sugestao")
                .build();
        wishlist.getItens().add(WishlistItem.builder().id(3L).nomeProduto("C").link("https://c.example").build());
        wishlist.getItens().add(WishlistItem.builder().id(1L).nomeProduto("A").link("https://a.example").build());
        wishlist.getItens().add(WishlistItem.builder().id(2L).nomeProduto("B").link("https://b.example").build());

        WishlistResponse response = mapper.toWishlistResponse(wishlist);

        assertThat(response.itens()).extracting("id").containsExactly(1L, 2L, 3L);
    }

    @Test
    void messageResponsePreservesAnonymityForReceiverOnly() {
        User sender = user(1L, "Ana");
        User receiver = user(2L, "Bruno");
        Group group = Group.builder().id(10L).nome("Grupo").dono(sender).build();
        Message message = Message.builder()
                .id(50L)
                .grupo(group)
                .remetente(sender)
                .destinatario(receiver)
                .conteudo("Mensagem")
                .dataEnvio(LocalDateTime.of(2026, 5, 30, 12, 0))
                .anonima(true)
                .build();

        MessageResponse senderView = mapper.toMessageResponse(message, sender.getId());
        MessageResponse receiverView = mapper.toMessageResponse(message, receiver.getId());

        assertThat(senderView.remetente()).isNotNull();
        assertThat(senderView.nomeRemetenteExibicao()).isEqualTo("Ana");
        assertThat(receiverView.remetente()).isNull();
        assertThat(receiverView.nomeRemetenteExibicao()).isEqualTo("amigo secreto");
    }

    private User user(Long id, String name) {
        return User.builder()
                .id(id)
                .nome(name)
                .email(name.toLowerCase() + "@example.com")
                .oauthId("oauth-" + id)
                .build();
    }
}
