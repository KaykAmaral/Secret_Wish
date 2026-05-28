package com.example.springApp.repository;

import com.example.springApp.dto.UnreadConversationCount;
import com.example.springApp.model.Draw;
import com.example.springApp.model.Group;
import com.example.springApp.model.Message;
import com.example.springApp.model.Notification;
import com.example.springApp.model.User;
import com.example.springApp.model.WishList;
import com.example.springApp.model.WishlistItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class RepositoryQueryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private DrawRepository drawRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private WishlistRepository wishlistRepository;

    @Test
    void groupRepositoryReturnsOnlyGroupsWhereUserIsMember() {
        User owner = user("Owner");
        User member = user("Member");
        User outsider = user("Outsider");
        Group group = group("Grupo", owner, owner, member);
        group("Outro grupo", outsider, outsider);

        assertThat(groupRepository.findByMembros_Id(member.getId()))
                .extracting(Group::getId)
                .containsExactly(group.getId());
        assertThat(groupRepository.findByIdAndMembros_Id(group.getId(), outsider.getId())).isEmpty();
        assertThat(groupRepository.existsByIdAndMembros_Id(group.getId(), member.getId())).isTrue();
    }

    @Test
    void messageRepositoryQueriesConversationUnreadCountsAndBulkReadUpdate() {
        User giver = user("Giver");
        User receiver = user("Receiver");
        User other = user("Other");
        Group group = group("Grupo", giver, giver, receiver, other);

        Message first = message(group, giver, receiver, "Primeira", false, LocalDateTime.now().minusMinutes(2));
        Message second = message(group, receiver, giver, "Resposta", false, LocalDateTime.now().minusMinutes(1));
        message(group, other, receiver, "Outra conversa", false, LocalDateTime.now());

        assertThat(messageRepository.findConversation(group.getId(), giver.getId(), receiver.getId()))
                .extracting(Message::getId)
                .containsExactly(first.getId(), second.getId());

        Map<Long, Long> unreadByPartner = messageRepository
                .countUnreadByConversationPartners(group.getId(), receiver.getId(), List.of(giver.getId(), other.getId()))
                .stream()
                .collect(Collectors.toMap(UnreadConversationCount::getOtherUserId, UnreadConversationCount::getUnreadCount));

        assertThat(unreadByPartner).containsEntry(giver.getId(), 1L).containsEntry(other.getId(), 1L);
        assertThat(messageRepository.markConversationReceivedMessagesAsRead(group.getId(), receiver.getId(), giver.getId()))
                .isEqualTo(1);
        assertThat(messageRepository.countByGrupoIdAndRemetenteIdAndDestinatarioIdAndLidaFalse(
                group.getId(), giver.getId(), receiver.getId()
        )).isZero();
    }

    @Test
    void drawRepositoryReturnsUserRelationsAndDirectPairChecks() {
        User owner = user("Owner");
        User memberA = user("Member A");
        User memberB = user("Member B");
        Group group = group("Grupo", owner, owner, memberA, memberB);
        draw(group, owner, memberA);
        draw(group, memberA, memberB);
        draw(group, memberB, owner);

        assertThat(drawRepository.findUserDrawRelations(group.getId(), memberA.getId()))
                .extracting(draw -> draw.getRemetente().getId())
                .containsExactlyInAnyOrder(owner.getId(), memberA.getId());
        assertThat(drawRepository.findByGrupo_IdAndRemetente_Id(group.getId(), owner.getId())).isPresent();
        assertThat(drawRepository.findByDestinatario_Id(owner.getId()))
                .extracting(draw -> draw.getRemetente().getId())
                .containsExactly(memberB.getId());
        assertThat(drawRepository.existsByGrupo_IdAndRemetente_IdAndDestinatario_Id(
                group.getId(), owner.getId(), memberA.getId()
        )).isTrue();
    }

    @Test
    void notificationRepositoryScopesReadsCountsAndBulkUpdatesByUser() {
        User user = user("User");
        User other = user("Other");
        Notification older = notification(user, "Antiga", false, LocalDateTime.now().minusDays(1));
        Notification newer = notification(user, "Nova", false, LocalDateTime.now());
        notification(other, "Outra pessoa", false, LocalDateTime.now().plusMinutes(1));

        assertThat(notificationRepository.findByUsuarioIdOrderByDataCriacaoDesc(user.getId()))
                .extracting(Notification::getId)
                .containsExactly(newer.getId(), older.getId());
        assertThat(notificationRepository.findByIdAndUsuarioId(newer.getId(), other.getId())).isEmpty();
        assertThat(notificationRepository.countByUsuarioIdAndLidaFalse(user.getId())).isEqualTo(2);
        assertThat(notificationRepository.markAllUnreadAsRead(user.getId())).isEqualTo(2);
        assertThat(notificationRepository.countByUsuarioIdAndLidaFalse(user.getId())).isZero();
        assertThat(notificationRepository.countByUsuarioIdAndLidaFalse(other.getId())).isEqualTo(1);
    }

    @Test
    void wishlistRepositoryFetchesItemsWithWishlist() {
        User user = user("Wishlist User");
        WishList wishlist = wishlistRepository.save(WishList.builder().usuario(user).build());
        wishlist.getItens().add(WishlistItem.builder()
                .wishlist(wishlist)
                .nomeProduto("Caneca")
                .link("https://example.com/caneca")
                .build());
        wishlistRepository.saveAndFlush(wishlist);

        assertThat(wishlistRepository.findByUsuarioIdWithItems(user.getId()))
                .isPresent()
                .get()
                .satisfies(found -> assertThat(found.getItens()).extracting(WishlistItem::getNomeProduto).containsExactly("Caneca"));
    }

    private User user(String name) {
        String normalized = name.toLowerCase().replace(" ", ".");
        return userRepository.save(User.builder()
                .nome(name)
                .email(normalized + "." + System.nanoTime() + "@example.com")
                .oauthId("oauth-" + normalized + "-" + System.nanoTime())
                .build());
    }

    private Group group(String name, User owner, User... members) {
        Group group = Group.builder()
                .nome(name)
                .descricao("Descricao")
                .codigoUnico(codeFrom(name))
                .dono(owner)
                .dataEvento(LocalDateTime.now().plusDays(30))
                .build();
        group.getMembros().addAll(List.of(members));
        return groupRepository.save(group);
    }

    private String codeFrom(String value) {
        return String.format("%04X-%04X", Math.abs(value.hashCode()) & 0xFFFF, System.nanoTime() & 0xFFFF);
    }

    private Draw draw(Group group, User giver, User receiver) {
        return drawRepository.save(Draw.builder()
                .grupo(group)
                .remetente(giver)
                .destinatario(receiver)
                .build());
    }

    private Message message(Group group, User sender, User receiver, String content, boolean read, LocalDateTime sentAt) {
        return messageRepository.save(Message.builder()
                .grupo(group)
                .remetente(sender)
                .destinatario(receiver)
                .conteudo(content)
                .lida(read)
                .dataEnvio(sentAt)
                .build());
    }

    private Notification notification(User user, String title, boolean read, LocalDateTime createdAt) {
        return notificationRepository.save(Notification.builder()
                .usuario(user)
                .titulo(title)
                .mensagem("Mensagem")
                .lida(read)
                .dataCriacao(createdAt)
                .build());
    }
}
