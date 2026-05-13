package com.example.springApp.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI secretWishOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Secret Wish API")
                        .version("v1")
                        .description("""
                                API do backend Secret Wish.

                                Autenticacao principal: OAuth2 Google em /oauth2/authorization/google.
                                Para testar endpoints protegidos pelo Swagger, informe um JWT no botao Authorize.
                                """))
                .addTagsItem(new Tag().name("Usuarios").description("Dados do usuario autenticado."))
                .addTagsItem(new Tag().name("Grupos").description("Criacao, entrada e gerenciamento de grupos."))
                .addTagsItem(new Tag().name("Wishlists").description("Itens desejados e sugestoes por IA."))
                .addTagsItem(new Tag().name("Sorteio").description("Execucao e consulta do amigo secreto."))
                .addTagsItem(new Tag().name("Mensagens").description("Mensagens privadas entre participantes."))
                .addTagsItem(new Tag().name("Notificacoes").description("Notificacoes e contadores de nao lidas."))
                .addTagsItem(new Tag().name("Desenvolvimento").description("Endpoints locais habilitados por configuracao."))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .components(new Components().addSecuritySchemes(
                        BEARER_AUTH,
                        new SecurityScheme()
                                .name(BEARER_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                ));
    }
}
