# Secret Wish API

Backend da plataforma Secret Wish, uma aplicacao para organizar amigo secreto com grupos, sorteio, wishlist, chat anonimo, notificacoes em tempo real, login com Google e sugestoes com IA.

## Stack

| Area | Tecnologia |
| --- | --- |
| Linguagem | Java 21 |
| Framework | Spring Boot 3.5.14 |
| Build | Maven Wrapper |
| API REST | Spring Web |
| Persistencia | Spring Data JPA, Hibernate |
| Banco local | H2 file-based em modo MySQL |
| Banco producao | MySQL 8+ |
| Migrations | Flyway |
| Seguranca | Spring Security, JWT, OAuth2 Client |
| Login social | Google OAuth2 |
| Tempo real | Spring WebSocket, STOMP, SockJS |
| Email | Spring Mail, SMTP Gmail |
| IA | Spring AI, Groq |
| Documentacao | Springdoc OpenAPI, Swagger UI |
| Validacao | Jakarta Bean Validation |
| Boilerplate | Lombok |
| Testes | JUnit 5, Spring Boot Test, MockMvc, Spring Security Test, H2 em memoria |

## Visao Geral

O backend expoe uma API REST protegida por JWT em cookie HTTP-only ou header `Authorization`. A aplicacao suporta cadastro/login por email e senha, login via Google OAuth2, criacao de grupos, entrada por codigo, sorteio, consulta de amigo secreto, wishlist, chat anonimo entre pares sorteados, notificacoes e sugestoes de presentes com IA.

O projeto tambem possui canais WebSocket para mensagens privadas, contadores de nao lidas e atualizacao de wishlist em tempo real.

## Arquitetura

```text
src/main/java/com/example/springApp
|-- config        # Security, CORS, OpenAPI, WebSocket, clock e validacoes de producao
|-- controller    # Controllers REST da API
|-- dto           # Requests e responses expostos ao frontend
|-- exception     # Excecoes de dominio e handler global
|-- mapper        # Conversao de entidades para DTOs
|-- model         # Entidades JPA
|-- repository    # Repositorios Spring Data JPA
|-- security      # JWT, filtro de autenticacao, OAuth2 Google
|-- service       # Regras de negocio
`-- websocket     # Principal STOMP
```

## Modulos Principais

- **Autenticacao**: cadastro, login local, logout, status da sessao, Google OAuth2 e JWT.
- **Usuarios**: consulta, atualizacao de perfil e exclusao de conta.
- **Grupos**: criacao, entrada por codigo, remocao de membros, saida e exclusao.
- **Sorteio**: ciclo de amigo secreto sem auto-sorteio, com permissao apenas para dono do grupo.
- **Wishlist**: lista pessoal de desejos, limite de 10 itens, visibilidade controlada pelo sorteio.
- **Mensagens**: chat anonimo permitido apenas entre pares do sorteio.
- **Notificacoes**: eventos persistidos para dashboard e contadores.
- **Tempo real**: mensagens, contadores e updates via STOMP.
- **IA**: sugestoes de presente com Groq usando apenas os itens reais da wishlist.
- **Email**: notificacoes de resultado de sorteio via SMTP quando habilitado.

## Requisitos

- Java 21
- Maven Wrapper incluso no projeto
- MySQL 8+ para uso com profile local/prod
- Google OAuth2 configurado, se login social for usado
- Chave Groq, se `AI_ENABLED=true`
- SMTP Gmail com senha de app, se `MAIL_ENABLED=true`

## Execucao Rapida em Desenvolvimento

O profile padrao usa H2 em arquivo e sobe sem MySQL:

```powershell
.\mvnw.cmd spring-boot:run
```

URLs principais:

- API: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui.html`
- Google OAuth2: `http://localhost:8080/oauth2/authorization/google`
- WebSocket nativo: `ws://localhost:8080/ws`
- SockJS: `http://localhost:8080/ws-sockjs`

## Execucao Local com MySQL

Crie o banco:

```sql
CREATE DATABASE secret_wish CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Copie o arquivo de exemplo:

```powershell
Copy-Item src\main\resources\application-local.example.properties src\main\resources\application-local.properties
```

Preencha os valores locais e execute:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

## Configuracao

### Desenvolvimento Padrao

`src/main/resources/application.properties` usa:

- H2 em arquivo: `./data/secret_wish`
- `spring.jpa.hibernate.ddl-auto=update`
- Flyway desabilitado para H2
- Swagger habilitado
- auth de desenvolvimento habilitado
- email e IA desabilitados por padrao

### Producao

`src/main/resources/application-prod.properties` usa defaults mais restritivos:

- datasource obrigatorio via env vars
- `spring.jpa.hibernate.ddl-auto=validate`
- Flyway habilitado
- Swagger desabilitado
- dev auth desabilitado
- cookie seguro habilitado
- validacao de configuracoes inseguras no startup

Variaveis obrigatorias em producao:

```properties
DB_URL=
DB_USERNAME=
DB_PASSWORD=
JWT_SECRET=
FRONTEND_ORIGIN=
FRONTEND_ORIGINS=
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
```

Variaveis opcionais:

```properties
GOOGLE_REDIRECT_URI={baseUrl}/login/oauth2/code/{registrationId}
MAIL_ENABLED=false
MAIL_USERNAME=
MAIL_PASSWORD=
AI_ENABLED=false
GROQ_API_KEY=
GROQ_BASE_URL=https://api.groq.com/openai
GROQ_AI_MODEL=llama-3.3-70b-versatile
AUTH_COOKIE_SAME_SITE=Lax
```

## Banco de Dados

As migrations ficam em:

```text
src/main/resources/db/migration
```

Tabelas principais:

- `users`
- `groups_table`
- `group_members`
- `draws`
- `messages`
- `notifications`
- `wishlists`
- `wishlist_items`

## Endpoints

### Autenticacao

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/status`
- `POST /api/logout`
- `GET /oauth2/authorization/google`
- `GET /login/oauth2/code/google`

### Usuario

- `GET /api/me`
- `PUT /api/me`
- `DELETE /api/me`

### Grupos

- `GET /api/groups`
- `GET /api/groups/{groupId}`
- `POST /api/groups`
- `POST /api/groups/join`
- `DELETE /api/groups/{groupId}/members/{memberId}`
- `DELETE /api/groups/{groupId}/leave`
- `DELETE /api/groups/{groupId}`

### Sorteio

- `POST /api/groups/{groupId}/draw`
- `GET /api/groups/{groupId}/draw/me`

### Wishlist

- `GET /api/wishlist`
- `POST /api/wishlist/items`
- `PUT /api/wishlist/items/{itemId}`
- `DELETE /api/wishlist/items/{itemId}`
- `GET /api/groups/{groupId}/users/{ownerId}/wishlist`
- `POST /api/groups/{groupId}/users/{ownerId}/wishlist/ai-suggestion`

### Mensagens

- `POST /api/groups/{groupId}/messages`
- `GET /api/groups/{groupId}/messages/{otherUserId}`
- `GET /api/groups/{groupId}/messages/chats`
- `PATCH /api/groups/{groupId}/messages/{otherUserId}/read`
- `GET /api/messages/unread-count`

### Notificacoes

- `GET /api/notifications`
- `GET /api/notifications/unread-count`
- `PATCH /api/notifications/{notificationId}/read`
- `PATCH /api/notifications/read-all`
- `DELETE /api/notifications/{notificationId}`
- `DELETE /api/notifications`

### Desenvolvimento Local

Disponiveis apenas com `app.dev-auth.enabled=true`:

- `GET /api/dev`
- `GET /api/dev/token`
- `POST /api/dev/token`
- `GET /api/dev/email/test`
- `POST /api/dev/email/test`

## WebSocket

Endpoints:

- `/ws`
- `/ws-sockjs`

Envie o JWT no `CONNECT`:

```text
Authorization: Bearer <token>
```

ou:

```text
access_token: <token>
```

Filas do usuario:

- `/user/queue/messages`
- `/user/queue/unread-count`
- `/user/queue/wishlist-update`

## Regras de Negocio Importantes

- Um usuario pode criar apenas um grupo.
- Grupo precisa de pelo menos 3 participantes para sorteio.
- Participantes nao podem entrar ou sair depois do sorteio.
- Apenas o dono pode sortear, remover membros e excluir o grupo.
- O sorteio cria pares sem que alguem tire a si mesmo.
- Chat e wishlist de terceiros so ficam disponiveis para pares permitidos pelo sorteio.
- Wishlist pessoal possui limite de 10 itens.
- Sugestao de IA exige pelo menos um item real na wishlist.
- Uso de sugestao por IA possui limite por janela de tempo.

## Contrato de Erro

Erros seguem um formato unico:

```json
{
  "timestamp": "2026-05-27T22:30:00",
  "status": 400,
  "error": "Erro de validacao",
  "message": "Existem campos invalidos na requisicao",
  "fields": {
    "nome": "must not be blank"
  }
}
```

Quando nao ha erros por campo, `fields` retorna `{}`.

## Testes

Rodar todos os testes:

```powershell
.\mvnw.cmd test
```

Rodar um teste especifico:

```powershell
.\mvnw.cmd -Dtest=ServiceRulesIntegrationTest#userCannotAddMoreThanTenWishlistItems test
```

Coberturas relevantes:

- regras de grupos e sorteio
- permissoes de wishlist e chat
- contrato global de erros
- CORS e seguranca
- validacoes de producao
- endpoints REST com MockMvc

## Teste Bruto de Performance Local

Foi executado um teste bruto local contra o backend em `http://127.0.0.1:8080`, usando H2 file-based e configuracao de desenvolvimento. O objetivo foi validar rapidamente latencia e throughput dos endpoints de leitura mais sensiveis apos as otimizacoes de paginacao, listagem leve de grupos e contadores.

Endpoints testados:

- `GET /api/auth/status`
- `GET /api/groups`
- `GET /api/notifications?page=0&size=50`
- `GET /api/notifications/unread-count`
- `GET /api/messages/unread-count`

Resultado com `Invoke-WebRequest`, 500 requisicoes concorrentes e concorrencia 25:

| Metrica | Resultado |
| --- | ---: |
| Requisicoes | 500 |
| Erros | 0 |
| Tempo total | 1.47 s |
| Throughput | 339.67 req/s |
| Latencia media | 4.75 ms |
| P95 | 10.04 ms |
| Maxima | 34.25 ms |

Resultado com `HttpClient`, 2.000 requisicoes concorrentes e concorrencia 50:

| Metrica | Resultado |
| --- | ---: |
| Requisicoes | 2.000 |
| Erros | 0 |
| Tempo total | 5.33 s |
| Throughput | 375.5 req/s |
| Latencia media | 5.02 ms |
| P50 | 4.14 ms |
| P95 | 10.11 ms |
| P99 | 14.15 ms |
| Maxima | 28.89 ms |

Os logs nao registraram erros durante a carga. O log SQL confirmou que `GET /api/groups` usa a query otimizada com `count`, e que notificacoes paginadas usam `fetch first ? rows only`.

Observacao: esses numeros nao representam producao. O teste foi local, com H2 e `spring.jpa.show-sql=true`; em producao, use MySQL, massa de dados realista, profile `prod`, logs SQL desabilitados e uma ferramenta dedicada como k6, JMeter ou Gatling.

## Documentacao OpenAPI

Em desenvolvimento:

```text
http://localhost:8080/swagger-ui.html
```

Em producao o Swagger fica desabilitado por padrao.

## Seguranca

- JWT assinado via `JWT_SECRET`.
- Cookie de autenticacao HTTP-only.
- `SameSite` e `Secure` configuraveis por ambiente.
- CORS restrito por `FRONTEND_ORIGIN` e `FRONTEND_ORIGINS`.
- OAuth2 Google integrado ao fluxo de sessao do backend.
- Endpoints `/api/dev/**` bloqueados em producao.
- Startup falha em `prod` se detectar configuracao insegura.

## Comandos Uteis

```powershell
# iniciar backend
.\mvnw.cmd spring-boot:run

# iniciar com profile prod
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=prod"

# rodar testes
.\mvnw.cmd test

# gerar pacote
.\mvnw.cmd clean package
```

## Observacoes

- Nao versionar `application-local.properties`.
- Nao usar secrets locais em producao.
- Use senha de app para SMTP Gmail.
- Para cookies cross-site, configure `AUTH_COOKIE_SAME_SITE=None` e `AUTH_COOKIE_SECURE=true`.
