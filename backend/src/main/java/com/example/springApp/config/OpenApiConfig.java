package com.example.springApp.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    /**
     * Centraliza metadados e componentes reutilizaveis da documentacao Swagger.
     */
    @Bean
    public OpenAPI secretWishOpenApi() {
        MediaType errorMediaType = new MediaType()
                .schema(new Schema<>().$ref("#/components/schemas/ApiErrorResponse"))
                .addExamples("validacao", new Example()
                        .summary("Erro de validacao")
                        .value("""
                                {
                                  "timestamp": "2026-05-28T01:00:00",
                                  "status": 400,
                                  "error": "Erro de validacao",
                                  "message": "Existem campos invalidos na requisicao",
                                  "fields": {
                                    "nome": "must not be blank"
                                  }
                                }
                                """))
                .addExamples("regraDeNegocio", new Example()
                        .summary("Erro de regra de negocio")
                        .value("""
                                {
                                  "timestamp": "2026-05-28T01:00:00",
                                  "status": 400,
                                  "error": "Regra de negocio violada",
                                  "message": "O grupo precisa ter pelo menos 3 participantes para o sorteio",
                                  "fields": {}
                                }
                                """));

        Components components = new Components()
                .addSecuritySchemes(
                        BEARER_AUTH,
                        new SecurityScheme()
                                .name(BEARER_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                )
                .addResponses("ErroPadrao", new ApiResponse()
                        .description("Contrato padrao de erro")
                        .content(new Content().addMediaType(
                                org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                                errorMediaType
                        )))
                .addResponses("NaoAutorizado", new ApiResponse()
                        .description("JWT ausente, invalido ou expirado")
                        .content(new Content().addMediaType(
                                org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                                errorMediaType
                        )))
                .addResponses("Proibido", new ApiResponse()
                        .description("Usuario autenticado sem permissao para executar a acao")
                        .content(new Content().addMediaType(
                                org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                                errorMediaType
                        )))
                .addResponses("NaoEncontrado", new ApiResponse()
                        .description("Recurso nao encontrado ou inacessivel para o usuario")
                        .content(new Content().addMediaType(
                                org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                                errorMediaType
                        )));

        return new OpenAPI()
                .info(new Info()
                        .title("Secret Wish API")
                        .version("v1")
                        .description("""
                                API REST do backend Secret Wish para amigo secreto, grupos, sorteios,
                                wishlist, mensagens anonimas, notificacoes em tempo real e sugestoes com IA.

                                Autenticacao:
                                - Login local retorna sessao por cookie HTTP-only.
                                - OAuth2 Google inicia em /oauth2/authorization/google.
                                - Para testar endpoints protegidos no Swagger, use Authorize com Bearer JWT.
                                """)
                        .contact(new Contact()
                                .name("Secret Wish")
                                .url("http://localhost:5173"))
                        .license(new License().name("Projeto academico/local")))
                .addTagsItem(new Tag().name("Autenticacao").description("Cadastro, login, status de sessao e logout."))
                .addTagsItem(new Tag().name("Usuarios").description("Perfil do usuario autenticado."))
                .addTagsItem(new Tag().name("Grupos").description("Criacao, entrada, listagem e gerenciamento de grupos."))
                .addTagsItem(new Tag().name("Wishlists").description("Itens desejados, visibilidade pos-sorteio e sugestoes por IA."))
                .addTagsItem(new Tag().name("Sorteio").description("Execucao do sorteio e consulta individual do amigo secreto."))
                .addTagsItem(new Tag().name("Mensagens").description("Chat privado e anonimo entre pares do sorteio."))
                .addTagsItem(new Tag().name("Notificacoes").description("Notificacoes persistidas e contadores de nao lidas."))
                .addTagsItem(new Tag().name("Desenvolvimento").description("Endpoints locais para token e email de teste."))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .components(components);
    }
}
