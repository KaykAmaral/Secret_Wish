package com.example.springApp;

import com.example.springApp.exception.BusinessException;
import com.example.springApp.exception.ConflictException;
import com.example.springApp.exception.ForbiddenException;
import com.example.springApp.exception.ResourceNotFoundException;
import com.example.springApp.model.Draw;
import com.example.springApp.model.Group;
import com.example.springApp.model.Message;
import com.example.springApp.model.Notification;
import com.example.springApp.model.User;
import com.example.springApp.model.WishList;
import com.example.springApp.model.WishlistItem;
import com.example.springApp.repository.DrawRepository;
import com.example.springApp.repository.GroupRepository;
import com.example.springApp.repository.MessageRepository;
import com.example.springApp.repository.NotificationRepository;
import com.example.springApp.repository.UserRepository;
import com.example.springApp.repository.WishlistItemRepository;
import com.example.springApp.repository.WishlistRepository;
import com.example.springApp.service.DrawService;
import com.example.springApp.service.GroupService;
import com.example.springApp.service.MessageService;
import com.example.springApp.service.NotificationService;
import com.example.springApp.service.AiSuggestionService;
import com.example.springApp.service.EmailService;
import com.example.springApp.service.WishlistService;
import com.example.springApp.security.JwtService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ServiceRulesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private DrawService drawService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AiSuggestionService aiSuggestionService;

    @Autowired
    private WishlistService wishlistService;

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

    @Autowired
    private WishlistItemRepository wishlistItemRepository;

    @BeforeEach
    void cleanDatabase() {
        messageRepository.deleteAll();
        notificationRepository.deleteAll();
        drawRepository.deleteAll();
        groupRepository.deleteAll();
        wishlistItemRepository.deleteAll();
        wishlistRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void userCannotCreateMoreThanOneGroup() {
        User owner = createUser("Owner");

        groupService.createGroup(newGroup("Grupo 1"), owner.getId());

        assertThatThrownBy(() -> groupService.createGroup(newGroup("Grupo 2"), owner.getId()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("ja possui um grupo");
    }

    @Test
    void groupCodeIsGeneratedByBackendAndOwnerBecomesMember() {
        User owner = createUser("Owner");

        Group group = groupService.createGroup(newGroup("Grupo"), owner.getId());

        assertThat(group.getCodigoUnico()).matches("^[A-Z0-9]{4}-[A-Z0-9]{4}$");
        assertThat(group.getMembros()).extracting(User::getId).containsExactly(owner.getId());
    }

    @Test
    void drawRequiresAtLeastThreeMembers() {
        User owner = createUser("Owner");
        User member = createUser("Member");
        Group group = groupService.createGroup(newGroup("Grupo"), owner.getId());
        groupService.joinGroup(group.getCodigoUnico(), member.getId());

        assertThatThrownBy(() -> drawService.performDraw(group.getId(), owner.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("pelo menos 3 participantes");
    }

    @Test
    void onlyOwnerCanPerformDraw() {
        Scenario scenario = createGroupWithThreeMembers();

        assertThatThrownBy(() -> drawService.performDraw(scenario.group().getId(), scenario.memberA().getId()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Apenas o dono");
    }

    @Test
    void performingDrawCreatesCycleAndMarksDrawDate() {
        Scenario scenario = createGroupWithThreeMembers();

        List<Draw> draws = drawService.performDraw(scenario.group().getId(), scenario.owner().getId());

        assertThat(draws).hasSize(3);
        assertThat(draws).allSatisfy(draw -> assertThat(draw.getRemetente().getId())
                .isNotEqualTo(draw.getDestinatario().getId()));
        assertThat(groupRepository.findById(scenario.group().getId()).orElseThrow().getDataSorteio()).isNotNull();
    }

    @Test
    void performingDrawCreatesNotificationsForParticipants() {
        Scenario scenario = createGroupWithThreeMembers();

        drawService.performDraw(scenario.group().getId(), scenario.owner().getId());

        assertThat(notificationService.getUserNotifications(scenario.owner().getId()))
                .hasSize(1)
                .first()
                .satisfies(notification -> {
                    assertThat(notification.getTitulo()).isEqualTo("Sorteio realizado");
                    assertThat(notification.isLida()).isFalse();
                });
        assertThat(notificationService.countUnreadNotifications(scenario.owner().getId())).isEqualTo(1);
    }

    @Test
    void redoingDrawDeletesPreviousDrawsAndGroupMessages() {
        Scenario scenario = createGroupWithThreeMembers();
        drawService.performDraw(scenario.group().getId(), scenario.owner().getId());
        Draw firstDraw = drawService.getMeuAmigoSecreto(scenario.group().getId(), scenario.owner().getId());
        messageService.sendMessage(
                scenario.group().getId(),
                firstDraw.getRemetente().getId(),
                firstDraw.getDestinatario().getId(),
                "Mensagem antiga"
        );

        List<Draw> newDraws = drawService.performDraw(scenario.group().getId(), scenario.owner().getId());

        assertThat(newDraws).hasSize(3);
        assertThat(drawRepository.findByGrupoId(scenario.group().getId())).hasSize(3);
        assertThat(messageRepository.findByGrupo_IdOrderByDataEnvioAsc(scenario.group().getId())).isEmpty();
    }

    @Test
    void participantCannotLeaveAfterDraw() {
        Scenario scenario = createGroupWithThreeMembers();
        drawService.performDraw(scenario.group().getId(), scenario.owner().getId());

        assertThatThrownBy(() -> groupService.leaveGroup(scenario.group().getId(), scenario.memberA().getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("sair depois do sorteio");
    }

    @Test
    void participantCannotJoinAfterDraw() {
        Scenario scenario = createGroupWithThreeMembers();
        User lateMember = createUser("Late Member");
        drawService.performDraw(scenario.group().getId(), scenario.owner().getId());

        assertThatThrownBy(() -> groupService.joinGroup(scenario.group().getCodigoUnico(), lateMember.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("entrar depois do sorteio");
    }

    @Test
    void ownerCanRemoveMemberBeforeDraw() {
        Scenario scenario = createGroupWithThreeMembers();

        Group group = groupService.removeMember(
                scenario.group().getId(),
                scenario.owner().getId(),
                scenario.memberA().getId()
        );

        assertThat(group.getMembros()).extracting(User::getId).doesNotContain(scenario.memberA().getId());
    }

    @Test
    void messagesCanOnlyBeExchangedBetweenDrawPairs() {
        Scenario scenario = createGroupWithThreeMembers();
        drawService.performDraw(scenario.group().getId(), scenario.owner().getId());
        Draw allowedPair = drawService.getMeuAmigoSecreto(scenario.group().getId(), scenario.owner().getId());
        User outsider = createUser("Outsider");

        Message message = messageService.sendMessage(
                scenario.group().getId(),
                allowedPair.getRemetente().getId(),
                allowedPair.getDestinatario().getId(),
                "Oi"
        );

        assertThat(message.getId()).isNotNull();
        assertThatThrownBy(() -> messageService.sendMessage(
                scenario.group().getId(),
                scenario.owner().getId(),
                outsider.getId(),
                "Nao pode"
        )).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void wishlistIsVisibleOnlyToWhoDrewTheUser() {
        Scenario scenario = createGroupWithThreeMembers();
        drawService.performDraw(scenario.group().getId(), scenario.owner().getId());
        Draw ownerDraw = drawService.getMeuAmigoSecreto(scenario.group().getId(), scenario.owner().getId());
        User notAllowedViewer = List.of(scenario.owner(), scenario.memberA(), scenario.memberB()).stream()
                .filter(user -> !user.getId().equals(ownerDraw.getRemetente().getId()))
                .filter(user -> !user.getId().equals(ownerDraw.getDestinatario().getId()))
                .findFirst()
                .orElseThrow();

        WishList visibleWishlist = wishlistService.getVisibleWishlist(
                scenario.group().getId(),
                scenario.owner().getId(),
                ownerDraw.getDestinatario().getId()
        );

        assertThat(visibleWishlist.getUsuario().getId()).isEqualTo(ownerDraw.getDestinatario().getId());
        assertThatThrownBy(() -> wishlistService.getVisibleWishlist(
                scenario.group().getId(),
                notAllowedViewer.getId(),
                ownerDraw.getDestinatario().getId()
        )).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void userCanUpdateAndRemoveOwnWishlistItems() {
        User user = createUser("User");
        WishlistItem item = wishlistService.addItemToWishlist(
                user.getId(),
                WishlistItem.builder().nomeProduto("Livro").link("https://example.com/livro").build()
        );

        WishlistItem updated = wishlistService.updateItem(
                item.getId(),
                user.getId(),
                WishlistItem.builder().nomeProduto("Livro 2").link("https://example.com/livro-2").build()
        );
        wishlistService.removeItemFromWishlist(updated.getId(), user.getId());

        assertThat(updated.getNomeProduto()).isEqualTo("Livro 2");
        assertThat(wishlistItemRepository.findById(updated.getId())).isEmpty();
    }

    @Test
    void aiSuggestionRequiresWishlistItems() {
        User user = createUser("User");
        WishList wishlist = wishlistService.getOrCreateWishlist(user.getId());

        assertThatThrownBy(() -> aiSuggestionService.generateSuggestion(wishlist, user.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("pelo menos um item");
    }

    @Test
    void aiSuggestionFailsGracefullyWhenDisabled() {
        User user = createUser("User");
        wishlistService.addItemToWishlist(
                user.getId(),
                WishlistItem.builder().nomeProduto("Livro").link("https://example.com/livro").build()
        );
        WishList wishlist = wishlistService.getOrCreateWishlist(user.getId());

        assertThatThrownBy(() -> aiSuggestionService.generateSuggestion(wishlist, user.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("desabilitadas");
        assertThat(wishlistRepository.findById(wishlist.getId()).orElseThrow().getSugestaoIa()).isNull();
        assertThat(aiSuggestionService.remainingGenerations(user.getId())).isEqualTo(3);
    }

    @Test
    void aiSuggestionUsageIsLimitedToThreeGenerationsPerHour() {
        User user = createUser("User");
        wishlistService.addItemToWishlist(
                user.getId(),
                WishlistItem.builder().nomeProduto("Livro").link("https://example.com/livro").build()
        );
        WishList wishlist = wishlistService.getOrCreateWishlist(user.getId());
        AiSuggestionService enabledService = new AiSuggestionService(
                unavailableAiProvider(),
                wishlistRepository,
                java.time.Clock.systemDefaultZone(),
                true
        );

        assertThat(enabledService.remainingGenerations(user.getId())).isEqualTo(3);
        assertThatThrownBy(() -> enabledService.generateSuggestion(wishlist, user.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cliente de IA");
        assertThat(enabledService.remainingGenerations(user.getId())).isEqualTo(2);
        assertThatThrownBy(() -> enabledService.generateSuggestion(wishlist, user.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cliente de IA");
        assertThat(enabledService.remainingGenerations(user.getId())).isEqualTo(1);
        assertThatThrownBy(() -> enabledService.generateSuggestion(wishlist, user.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cliente de IA");

        assertThat(enabledService.remainingGenerations(user.getId())).isZero();
        assertThatThrownBy(() -> enabledService.generateSuggestion(wishlist, user.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Limite de 3 sugestoes");
    }

    @Test
    void aiSuggestionUsageLimitIsIndependentPerUser() {
        User user = createUser("User");
        User otherUser = createUser("Other User");
        wishlistService.addItemToWishlist(
                user.getId(),
                WishlistItem.builder().nomeProduto("Livro").link("https://example.com/livro").build()
        );
        WishList wishlist = wishlistService.getOrCreateWishlist(user.getId());
        AiSuggestionService enabledService = new AiSuggestionService(
                unavailableAiProvider(),
                wishlistRepository,
                java.time.Clock.systemDefaultZone(),
                true
        );

        for (int attempt = 0; attempt < 3; attempt++) {
            assertThatThrownBy(() -> enabledService.generateSuggestion(wishlist, user.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Cliente de IA");
        }

        assertThat(enabledService.remainingGenerations(user.getId())).isZero();
        assertThat(enabledService.remainingGenerations(otherUser.getId())).isEqualTo(3);
    }

    @Test
    void aiSuggestionEndpointReturnsBusinessErrorWithoutBreakingWhenWishlistIsEmpty() throws Exception {
        Scenario scenario = createGroupWithThreeMembers();
        drawService.performDraw(scenario.group().getId(), scenario.owner().getId());
        Draw ownerDraw = drawService.getMeuAmigoSecreto(scenario.group().getId(), scenario.owner().getId());

        mockMvc.perform(post("/api/groups/{groupId}/users/{ownerId}/wishlist/ai-suggestion",
                        scenario.group().getId(),
                        ownerDraw.getDestinatario().getId()
                )
                        .header("Authorization", bearerToken(ownerDraw.getRemetente())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A wishlist precisa ter pelo menos um item para gerar sugestoes"));
    }

    @Test
    void aiSuggestionEndpointReturnsBusinessErrorWithoutCallingAiWhenDisabled() throws Exception {
        Scenario scenario = createGroupWithThreeMembers();
        drawService.performDraw(scenario.group().getId(), scenario.owner().getId());
        Draw ownerDraw = drawService.getMeuAmigoSecreto(scenario.group().getId(), scenario.owner().getId());
        wishlistService.addItemToWishlist(
                ownerDraw.getDestinatario().getId(),
                WishlistItem.builder().nomeProduto("Livro").link("https://example.com/livro").build()
        );

        mockMvc.perform(post("/api/groups/{groupId}/users/{ownerId}/wishlist/ai-suggestion",
                        scenario.group().getId(),
                        ownerDraw.getDestinatario().getId()
                )
                        .header("Authorization", bearerToken(ownerDraw.getRemetente())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Sugestoes com IA estao desabilitadas neste ambiente"));
    }

    @Test
    void aiSuggestionEndpointRejectsUserWhoCannotSeeWishlist() throws Exception {
        Scenario scenario = createGroupWithThreeMembers();
        drawService.performDraw(scenario.group().getId(), scenario.owner().getId());
        Draw ownerDraw = drawService.getMeuAmigoSecreto(scenario.group().getId(), scenario.owner().getId());
        User notAllowedViewer = List.of(scenario.owner(), scenario.memberA(), scenario.memberB()).stream()
                .filter(user -> !user.getId().equals(ownerDraw.getRemetente().getId()))
                .filter(user -> !user.getId().equals(ownerDraw.getDestinatario().getId()))
                .findFirst()
                .orElseThrow();

        mockMvc.perform(post("/api/groups/{groupId}/users/{ownerId}/wishlist/ai-suggestion",
                        scenario.group().getId(),
                        ownerDraw.getDestinatario().getId()
                )
                        .header("Authorization", bearerToken(notAllowedViewer)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Voce nao pode visualizar esta wishlist"));
    }

    @Test
    void testEmailFailsGracefullyWhenMailIsDisabled() {
        EmailService disabledEmailService = new EmailService(unavailableMailProvider(), false, "");

        assertThatThrownBy(() -> disabledEmailService.sendTestEmail("user@example.com"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("desabilitado");
    }

    @Test
    void testEmailRejectsInvalidRecipient() {
        EmailService disabledEmailService = new EmailService(unavailableMailProvider(), false, "");

        assertThatThrownBy(() -> disabledEmailService.sendTestEmail("email-invalido"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("invalido");
    }

    @Test
    void testEmailUsesConfiguredMailSender() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        EmailService enabledEmailService = new EmailService(availableMailProvider(mailSender), true, "sender@example.com");

        enabledEmailService.sendTestEmail("user@example.com");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void protectedEndpointRequiresJwt() throws Exception {
        mockMvc.perform(get("/api/groups"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Nao autenticado"))
                .andExpect(jsonPath("$.message").value("Autenticacao obrigatoria para acessar este recurso"))
                .andExpect(jsonPath("$.fields").isMap());
    }

    @Test
    void devEndpointRequiresJwtWhenDevAuthIsDisabledByDefault() throws Exception {
        mockMvc.perform(get("/api/dev"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.fields").isMap());
    }

    @Test
    void authStatusReturnsAnonymousWhenJwtIsMissing() throws Exception {
        mockMvc.perform(get("/api/auth/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(false))
                .andExpect(jsonPath("$.user").doesNotExist());
    }

    @Test
    void authStatusReturnsUserWhenJwtIsValid() throws Exception {
        User user = createUser("Session User");

        mockMvc.perform(get("/api/auth/status")
                        .header("Authorization", bearerToken(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.user.id").value(user.getId()))
                .andExpect(jsonPath("$.user.email").value(user.getEmail()));
    }

    @Test
    void logoutClearsAuthenticationCookieWithoutRequiringJwt() throws Exception {
        mockMvc.perform(post("/api/logout"))
                .andExpect(status().isNoContent())
                .andExpect(result -> assertThat(result.getResponse().getHeader(HttpHeaders.SET_COOKIE))
                        .contains("secret_wish_token=")
                        .contains("Max-Age=0"));
    }

    @Test
    void sockJsInfoEndpointIsPublicForHandshake() throws Exception {
        mockMvc.perform(get("/ws-sockjs/info"))
                .andExpect(status().isOk());
    }

    @Test
    void validationErrorsUseStandardErrorContract() throws Exception {
        User user = createUser("Validation User");

        mockMvc.perform(post("/api/groups")
                        .header("Authorization", bearerToken(user))
                        .contentType("application/json")
                        .content("""
                                {
                                  "nome": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Erro de validacao"))
                .andExpect(jsonPath("$.message").value("Existem campos invalidos na requisicao"))
                .andExpect(jsonPath("$.fields.nome").exists());
    }

    @Test
    void invalidPathParameterUsesStandardErrorContract() throws Exception {
        User user = createUser("Invalid Parameter User");

        mockMvc.perform(get("/api/groups/not-a-number/draw/me")
                        .header("Authorization", bearerToken(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Parametro invalido"))
                .andExpect(jsonPath("$.message").value("Parametro 'groupId' possui valor invalido"))
                .andExpect(jsonPath("$.fields").isMap());
    }

    @Test
    void drawEndpointDoesNotExposePairings() throws Exception {
        Scenario scenario = createGroupWithThreeMembers();

        mockMvc.perform(post("/api/groups/{groupId}/draw", scenario.group().getId())
                        .header("Authorization", bearerToken(scenario.owner())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupId").value(scenario.group().getId()))
                .andExpect(jsonPath("$.participantCount").value(3))
                .andExpect(jsonPath("$.remetente").doesNotExist())
                .andExpect(jsonPath("$.destinatario").doesNotExist())
                .andExpect(jsonPath("$[0]").doesNotExist());
    }

    @Test
    void wishlistEndpointRejectsUserWhoCannotSeeWishlist() throws Exception {
        Scenario scenario = createGroupWithThreeMembers();
        drawService.performDraw(scenario.group().getId(), scenario.owner().getId());
        Draw ownerDraw = drawService.getMeuAmigoSecreto(scenario.group().getId(), scenario.owner().getId());
        User notAllowedViewer = List.of(scenario.owner(), scenario.memberA(), scenario.memberB()).stream()
                .filter(user -> !user.getId().equals(ownerDraw.getRemetente().getId()))
                .filter(user -> !user.getId().equals(ownerDraw.getDestinatario().getId()))
                .findFirst()
                .orElseThrow();

        mockMvc.perform(get("/api/groups/{groupId}/users/{ownerId}/wishlist",
                        scenario.group().getId(),
                        ownerDraw.getDestinatario().getId()
                )
                        .header("Authorization", bearerToken(notAllowedViewer)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Voce nao pode visualizar esta wishlist"));
    }

    @Test
    void wishlistEndpointsReturnItemsWithOpenInViewDisabled() throws Exception {
        Scenario scenario = createGroupWithThreeMembers();
        wishlistService.addItemToWishlist(
                scenario.memberA().getId(),
                WishlistItem.builder().nomeProduto("Caneca").link("https://example.com/caneca").build()
        );
        drawService.performDraw(scenario.group().getId(), scenario.owner().getId());
        Draw draw = drawRepository.findByGrupoId(scenario.group().getId()).stream()
                .filter(candidate -> candidate.getDestinatario().getId().equals(scenario.memberA().getId()))
                .findFirst()
                .orElseThrow();

        mockMvc.perform(get("/api/wishlist")
                        .header("Authorization", bearerToken(scenario.memberA())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].nomeProduto").value("Caneca"));

        mockMvc.perform(get("/api/groups/{groupId}/draw/me", scenario.group().getId())
                        .header("Authorization", bearerToken(draw.getRemetente())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wishlist.itens[0].nomeProduto").value("Caneca"));
    }

    @Test
    void messageConversationHidesSenderFromRecipient() throws Exception {
        Scenario scenario = createGroupWithThreeMembers();
        drawService.performDraw(scenario.group().getId(), scenario.owner().getId());
        Draw ownerDraw = drawService.getMeuAmigoSecreto(scenario.group().getId(), scenario.owner().getId());
        messageService.sendMessage(
                scenario.group().getId(),
                ownerDraw.getRemetente().getId(),
                ownerDraw.getDestinatario().getId(),
                "Mensagem anonima"
        );

        mockMvc.perform(get("/api/groups/{groupId}/messages/{otherUserId}",
                        scenario.group().getId(),
                        ownerDraw.getRemetente().getId()
                )
                        .header("Authorization", bearerToken(ownerDraw.getDestinatario())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].remetente").doesNotExist())
                .andExpect(jsonPath("$[0].nomeRemetenteExibicao").value("amigo secreto"))
                .andExpect(jsonPath("$[0].conteudo").value("Mensagem anonima"));
    }

    @Test
    void messageEndpointRejectsUsersOutsideDrawPair() throws Exception {
        ScenarioWithFourMembers scenario = createGroupWithFourMembers();
        drawService.performDraw(scenario.group().getId(), scenario.owner().getId());
        Draw ownerDraw = drawService.getMeuAmigoSecreto(scenario.group().getId(), scenario.owner().getId());
        User notAllowedViewer = List.of(scenario.owner(), scenario.memberA(), scenario.memberB(), scenario.memberC()).stream()
                .filter(user -> !canExchangeMessages(scenario.group().getId(), user.getId(), ownerDraw.getDestinatario().getId()))
                .findFirst()
                .orElseThrow();

        mockMvc.perform(get("/api/groups/{groupId}/messages/{otherUserId}",
                        scenario.group().getId(),
                        ownerDraw.getDestinatario().getId()
                )
                        .header("Authorization", bearerToken(notAllowedViewer)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Voce nao pode acessar esta conversa"));
    }

    @Test
    void notificationEndpointReturnsOnlyAuthenticatedUserNotifications() throws Exception {
        User user = createUser("User");
        User otherUser = createUser("Other User");
        notificationService.createNotification(user.getId(), "Minha notificacao", "Mensagem");
        notificationService.createNotification(otherUser.getId(), "Outra notificacao", "Mensagem");

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", bearerToken(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].titulo").value("Minha notificacao"));
    }

    @Test
    void notificationEndpointRejectsMarkingAnotherUsersNotificationAsRead() throws Exception {
        User user = createUser("User");
        User otherUser = createUser("Other User");
        Notification notification = notificationService.createNotification(otherUser.getId(), "Outra notificacao", "Mensagem");

        mockMvc.perform(patch("/api/notifications/{notificationId}/read", notification.getId())
                        .header("Authorization", bearerToken(user)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Notificacao nao encontrada"));
    }

    @Test
    void userCanReadAndDeleteOwnNotificationsOnly() {
        User user = createUser("User");
        User otherUser = createUser("Other User");
        Notification notification = notificationService.createNotification(user.getId(), "Titulo", "Mensagem");

        Notification readNotification = notificationService.markAsRead(notification.getId(), user.getId());

        assertThat(readNotification.isLida()).isTrue();
        assertThat(notificationService.countUnreadNotifications(user.getId())).isZero();
        assertThatThrownBy(() -> notificationService.markAsRead(notification.getId(), otherUser.getId()))
                .isInstanceOf(ResourceNotFoundException.class);

        notificationService.deleteNotification(notification.getId(), user.getId());

        assertThat(notificationRepository.findById(notification.getId())).isEmpty();
    }

    private Scenario createGroupWithThreeMembers() {
        User owner = createUser("Owner");
        User memberA = createUser("Member A");
        User memberB = createUser("Member B");
        Group group = groupService.createGroup(newGroup("Grupo"), owner.getId());
        groupService.joinGroup(group.getCodigoUnico(), memberA.getId());
        groupService.joinGroup(group.getCodigoUnico(), memberB.getId());
        return new Scenario(owner, memberA, memberB, group);
    }

    private ScenarioWithFourMembers createGroupWithFourMembers() {
        User owner = createUser("Owner");
        User memberA = createUser("Member A");
        User memberB = createUser("Member B");
        User memberC = createUser("Member C");
        Group group = groupService.createGroup(newGroup("Grupo"), owner.getId());
        groupService.joinGroup(group.getCodigoUnico(), memberA.getId());
        groupService.joinGroup(group.getCodigoUnico(), memberB.getId());
        groupService.joinGroup(group.getCodigoUnico(), memberC.getId());
        return new ScenarioWithFourMembers(owner, memberA, memberB, memberC, group);
    }

    private boolean canExchangeMessages(Long groupId, Long userId, Long otherUserId) {
        return drawRepository.existsByGrupo_IdAndRemetente_IdAndDestinatario_Id(groupId, userId, otherUserId)
                || drawRepository.existsByGrupo_IdAndRemetente_IdAndDestinatario_Id(groupId, otherUserId, userId);
    }

    private User createUser(String name) {
        String normalized = name.toLowerCase().replace(" ", ".");
        return userRepository.save(User.builder()
                .nome(name)
                .email(normalized + "@example.com")
                .oauthId("oauth-" + normalized)
                .build());
    }

    private Group newGroup(String name) {
        return Group.builder()
                .nome(name)
                .dataEvento(LocalDateTime.now().plusDays(30))
                .build();
    }

    private String bearerToken(User user) {
        return "Bearer " + jwtService.generateToken(user.getId(), user.getEmail(), user.getNome());
    }

    private ObjectProvider<org.springframework.ai.chat.client.ChatClient.Builder> unavailableAiProvider() {
        return new ObjectProvider<>() {
            @Override
            public org.springframework.ai.chat.client.ChatClient.Builder getObject(Object... args) {
                return null;
            }

            @Override
            public org.springframework.ai.chat.client.ChatClient.Builder getIfAvailable() {
                return null;
            }

            @Override
            public org.springframework.ai.chat.client.ChatClient.Builder getIfUnique() {
                return null;
            }

            @Override
            public org.springframework.ai.chat.client.ChatClient.Builder getObject() {
                return null;
            }
        };
    }

    private ObjectProvider<JavaMailSender> unavailableMailProvider() {
        return new ObjectProvider<>() {
            @Override
            public JavaMailSender getObject(Object... args) {
                return null;
            }

            @Override
            public JavaMailSender getIfAvailable() {
                return null;
            }

            @Override
            public JavaMailSender getIfUnique() {
                return null;
            }

            @Override
            public JavaMailSender getObject() {
                return null;
            }
        };
    }

    private ObjectProvider<JavaMailSender> availableMailProvider(JavaMailSender mailSender) {
        return new ObjectProvider<>() {
            @Override
            public JavaMailSender getObject(Object... args) {
                return mailSender;
            }

            @Override
            public JavaMailSender getIfAvailable() {
                return mailSender;
            }

            @Override
            public JavaMailSender getIfUnique() {
                return mailSender;
            }

            @Override
            public JavaMailSender getObject() {
                return mailSender;
            }
        };
    }

    private record Scenario(User owner, User memberA, User memberB, Group group) {
    }

    private record ScenarioWithFourMembers(User owner, User memberA, User memberB, User memberC, Group group) {
    }
}
