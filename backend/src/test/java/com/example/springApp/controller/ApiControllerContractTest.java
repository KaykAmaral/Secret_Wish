package com.example.springApp.controller;

import com.example.springApp.model.User;
import com.example.springApp.model.WishlistItem;
import com.example.springApp.repository.DrawRepository;
import com.example.springApp.repository.GroupRepository;
import com.example.springApp.repository.MessageRepository;
import com.example.springApp.repository.NotificationRepository;
import com.example.springApp.repository.UserRepository;
import com.example.springApp.repository.WishlistItemRepository;
import com.example.springApp.repository.WishlistRepository;
import com.example.springApp.security.JwtService;
import com.example.springApp.service.WishlistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockCookie;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

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
    void protectedApiEndpointReturnsStandardUnauthorizedJsonWhenJwtIsMissing() throws Exception {
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Nao autenticado"))
                .andExpect(jsonPath("$.message").value("Autenticacao obrigatoria para acessar este recurso"))
                .andExpect(jsonPath("$.fields").isMap());
    }

    @Test
    void registerEndpointCreatesLocalUserAndSetsHttpOnlyCookie() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("""
                                {
                                  "nome": "Usuario Local",
                                  "email": "local.contract@example.com",
                                  "password": "secret123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.user.email").value("local.contract@example.com"))
                .andExpect(result -> assertThat(result.getResponse().getHeader(HttpHeaders.SET_COOKIE))
                        .contains("secret_wish_token=")
                        .contains("HttpOnly")
                        .contains("SameSite=Lax"));

        assertThat(userRepository.findByEmail("local.contract@example.com")).isPresent();
    }

    @Test
    void wishlistItemCreationReturnsBusinessErrorWhenUserAlreadyHasTenItems() throws Exception {
        User user = createUser("Wishlist Limit User");

        for (int index = 1; index <= 10; index++) {
            wishlistService.addItemToWishlist(
                    user.getId(),
                    WishlistItem.builder()
                            .nomeProduto("Presente " + index)
                            .link("https://example.com/presente-" + index)
                            .build()
            );
        }

        mockMvc.perform(post("/api/wishlist/items")
                        .header("Authorization", bearerToken(user))
                        .contentType("application/json")
                        .content("""
                                {
                                  "nomeProduto": "Presente 11",
                                  "link": "https://example.com/presente-11"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("A lista de desejos pode ter no maximo 10 itens"));
    }

    @Test
    void cookieAuthenticatedMutationRequiresTrustedOrigin() throws Exception {
        User user = createUser("Cookie Origin User");
        MockCookie authCookie = new MockCookie("secret_wish_token", jwtService.generateToken(
                user.getId(),
                user.getEmail(),
                user.getNome()
        ));

        mockMvc.perform(post("/api/wishlist/items")
                        .cookie(authCookie)
                        .header(HttpHeaders.REFERER, "http://malicious.localhost/wishlist")
                        .contentType("application/json")
                        .content("""
                                {
                                  "nomeProduto": "Caneca",
                                  "link": "https://example.com/caneca"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Origem nao permitida"));
    }

    @Test
    void cookieAuthenticatedMutationAllowsConfiguredFrontendOrigin() throws Exception {
        User user = createUser("Cookie Valid Origin User");
        MockCookie authCookie = new MockCookie("secret_wish_token", jwtService.generateToken(
                user.getId(),
                user.getEmail(),
                user.getNome()
        ));

        mockMvc.perform(post("/api/wishlist/items")
                        .cookie(authCookie)
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .contentType("application/json")
                        .content("""
                                {
                                  "nomeProduto": "Caneca",
                                  "link": "https://example.com/caneca"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nomeProduto").value("Caneca"));
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
