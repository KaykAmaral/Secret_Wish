package com.example.springApp;

import com.example.springApp.model.Draw;
import com.example.springApp.model.User;
import com.example.springApp.repository.DrawRepository;
import com.example.springApp.repository.GroupRepository;
import com.example.springApp.repository.MessageRepository;
import com.example.springApp.repository.NotificationRepository;
import com.example.springApp.repository.UserRepository;
import com.example.springApp.repository.WishlistItemRepository;
import com.example.springApp.repository.WishlistRepository;
import com.example.springApp.security.JwtService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecretWishApiFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

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
    void userCanCreateGroupJoinMembersDrawSeeWishlistAndExchangeMessageThroughApi() throws Exception {
        User owner = createUser("Flow Owner");
        User memberA = createUser("Flow Member A");
        User memberB = createUser("Flow Member B");

        JsonNode createdGroup = json(mockMvc.perform(post("/api/groups")
                        .header("Authorization", bearerToken(owner))
                        .contentType("application/json")
                        .content("""
                                {
                                  "nome": "Amigo secreto API",
                                  "descricao": "Fluxo completo via API",
                                  "dataEvento": "%s"
                                }
                                """.formatted(LocalDateTime.now().plusDays(30))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigoUnico").exists())
                .andReturn()
                .getResponse()
                .getContentAsString());

        Long groupId = createdGroup.get("id").asLong();
        String code = createdGroup.get("codigoUnico").asText();

        joinGroup(memberA, code);
        joinGroup(memberB, code);

        mockMvc.perform(post("/api/wishlist/items")
                        .header("Authorization", bearerToken(memberA))
                        .contentType("application/json")
                        .content("""
                                {
                                  "nomeProduto": "Livro",
                                  "link": "https://example.com/livro"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nomeProduto").value("Livro"));

        mockMvc.perform(post("/api/groups/{groupId}/draw", groupId)
                        .header("Authorization", bearerToken(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participantCount").value(3));

        Draw drawToMemberA = drawRepository.findByGrupoId(groupId).stream()
                .filter(draw -> draw.getDestinatario().getId().equals(memberA.getId()))
                .findFirst()
                .orElseThrow();
        User giver = drawToMemberA.getRemetente();

        mockMvc.perform(get("/api/groups/{groupId}/draw/me", groupId)
                        .header("Authorization", bearerToken(giver)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amigoSecreto.id").value(memberA.getId()))
                .andExpect(jsonPath("$.wishlist.itens[0].nomeProduto").value("Livro"));

        mockMvc.perform(get("/api/groups/{groupId}/users/{ownerId}/wishlist", groupId, memberA.getId())
                        .header("Authorization", bearerToken(giver)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].nomeProduto").value("Livro"));

        mockMvc.perform(post("/api/groups/{groupId}/messages", groupId)
                        .header("Authorization", bearerToken(giver))
                        .contentType("application/json")
                        .content("""
                                {
                                  "destinatarioId": %d,
                                  "conteudo": "Qual cor voce prefere?"
                                }
                                """.formatted(memberA.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.conteudo").value("Qual cor voce prefere?"));

        mockMvc.perform(get("/api/messages/unread-count")
                        .header("Authorization", bearerToken(memberA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(1));

        mockMvc.perform(patch("/api/groups/{groupId}/messages/{otherUserId}/read", groupId, giver.getId())
                        .header("Authorization", bearerToken(memberA)))
                .andExpect(status().isNoContent());

        assertThat(messageRepository.countByDestinatarioIdAndLidaFalse(memberA.getId())).isZero();
    }

    private void joinGroup(User user, String code) throws Exception {
        mockMvc.perform(post("/api/groups/join")
                        .header("Authorization", bearerToken(user))
                        .contentType("application/json")
                        .content("""
                                {
                                  "codigoUnico": "%s"
                                }
                                """.formatted(code)))
                .andExpect(status().isOk());
    }

    private JsonNode json(String content) throws Exception {
        return objectMapper.readTree(content);
    }

    private User createUser(String name) {
        String normalized = name.toLowerCase().replace(" ", ".");
        return userRepository.save(User.builder()
                .nome(name)
                .email(normalized + "@example.com")
                .oauthId("oauth-" + normalized)
                .build());
    }

    private String bearerToken(User user) {
        return "Bearer " + jwtService.generateToken(user.getId(), user.getEmail(), user.getNome());
    }
}
