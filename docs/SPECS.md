# Secret Wish - Especificacoes do Projeto

## Stack

- Linguagem: Java 21
- Framework: Spring Boot
- Banco principal: MySQL
- Banco de testes: H2 em modo MySQL
- Migracoes: Flyway
- Autenticacao: Google OAuth2 + JWT
- Documentacao: Springdoc OpenAPI/Swagger
- Tempo real: WebSocket/STOMP
- Email: SMTP Gmail
- IA: Spring AI/OpenAI, quando habilitado

## Perfis e Configuracao

### Profile Local

Arquivo local esperado:

```text
src/main/resources/application-local.properties
```

Esse arquivo deve ficar fora do Git.

Principais variaveis:

```properties
DB_URL=jdbc:mysql://localhost:3306/secret_wish
DB_USERNAME=root
DB_PASSWORD=...
JWT_SECRET=...
DEV_AUTH_ENABLED=true
SWAGGER_ENABLED=true
FRONTEND_ORIGIN=http://localhost:3000
FRONTEND_ORIGINS=http://localhost:3000,http://localhost:5173
GOOGLE_CLIENT_ID=...
GOOGLE_CLIENT_SECRET=...
MAIL_ENABLED=false
AI_ENABLED=false
```

### Profile Producao

Profile:

```text
prod
```

Arquivo:

```text
src/main/resources/application-prod.properties
```

Obrigatorio em producao:

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

Obrigatorio apenas quando habilitado:

```properties
MAIL_ENABLED=true
MAIL_USERNAME=
MAIL_PASSWORD=

AI_ENABLED=true
OPENAI_API_KEY=
```

Defaults de producao:

- `app.dev-auth.enabled=false`
- `springdoc.api-docs.enabled=false`
- `springdoc.swagger-ui.enabled=false`
- `app.auth.cookie-secure=true`
- `spring.jpa.hibernate.ddl-auto=validate`
- `spring.flyway.enabled=true`
- `server.forward-headers-strategy=framework`

## Autenticacao

### Login Google

Inicio:

```http
GET /oauth2/authorization/google
```

Callback:

```http
GET /login/oauth2/code/google
```

Apos sucesso:

- backend cria ou atualiza o usuario;
- backend gera JWT;
- backend grava cookie `secret_wish_token`;
- backend redireciona para `{FRONTEND_ORIGIN}/oauth2/callback`.

Cookie:

- nome: `secret_wish_token`
- `HttpOnly=true`
- `Secure=true` em producao
- `SameSite=Lax` por padrao
- validade: 1 hora
- path: `/`

### JWT

Algoritmo:

```text
HS256
```

Claims:

- `sub`: ID do usuario
- `email`: email do usuario
- `name`: nome do usuario
- `iat`: emissao
- `exp`: expiracao

Expiracao padrao:

```text
60 minutos
```

### Status de Sessao

```http
GET /api/auth/status
```

Autenticado:

```json
{
  "authenticated": true,
  "user": {
    "id": 1,
    "nome": "Kayky",
    "email": "kayky@example.com"
  }
}
```

Nao autenticado:

```json
{
  "authenticated": false,
  "user": null
}
```

### Logout

```http
POST /api/logout
```

Resposta:

```text
204 No Content
```

## Contrato de Erro

Formato padrao:

```json
{
  "timestamp": "2026-05-14T00:00:00",
  "status": 400,
  "error": "Erro de validacao",
  "message": "Existem campos invalidos na requisicao",
  "fields": {
    "nome": "must not be blank"
  }
}
```

Regras:

- `fields` sempre existe.
- Em erros sem campos, `fields` vem como `{}`.
- Erros internos nao expõem stacktrace.

Status principais:

- `400`: regra de negocio, corpo invalido ou validacao.
- `401`: autenticacao ausente/invalida.
- `403`: usuario autenticado sem permissao.
- `404`: recurso nao encontrado.
- `409`: conflito.
- `429`: limite de uso atingido.
- `500`: erro interno inesperado.

## Modelo de Dados

### User

Campos:

- `id`
- `nome`
- `email`
- `oauthId`

Regras:

- `email` unico.
- `oauthId` unico.
- `oauthId` nao e exposto em DTOs publicos.

### Group

Campos:

- `id`
- `nome`
- `codigoUnico`
- `dono`
- `membros`
- `dataCriacao`
- `dataSorteio`
- `dataEvento`

Regras:

- `codigoUnico` unico.
- `codigoUnico` segue formato `XXXX-XXXX`.
- `dono` e unico por usuario.
- dono tambem e membro.

### Draw

Campos:

- `id`
- `grupo`
- `remetente`
- `destinatario`

Regras:

- por grupo, cada remetente aparece uma vez.
- por grupo, cada destinatario aparece uma vez.
- sorteio e circular.

### WishList

Campos:

- `id`
- `usuario`
- `sugestaoIa`
- `itens`

Regras:

- uma wishlist por usuario.
- criada automaticamente quando consultada.

### WishlistItem

Campos:

- `id`
- `wishlist`
- `nomeProduto`
- `link`

### Message

Campos:

- `id`
- `grupo`
- `remetente`
- `destinatario`
- `conteudo`
- `dataEnvio`
- `lida`
- `anonima`

Regras:

- sempre privada.
- enviada apenas entre pares relacionados pelo sorteio.
- remetente real fica oculto para o destinatario.

### Notification

Campos:

- `id`
- `usuario`
- `titulo`
- `mensagem`
- `dataCriacao`
- `lida`

## Endpoints

Todos os endpoints `/api/**` exigem JWT, exceto:

- `GET /api/auth/status`
- `POST /api/logout`
- `/api/dev/**`, apenas quando habilitado localmente

### Autenticacao

```http
GET /api/auth/status
POST /api/logout
GET /api/me
```

### Grupos

```http
GET /api/groups
GET /api/groups/{groupId}
POST /api/groups
POST /api/groups/join
DELETE /api/groups/{groupId}/members/{memberId}
DELETE /api/groups/{groupId}/leave
DELETE /api/groups/{groupId}
```

Criar grupo:

```json
{
  "nome": "Amigo secreto da familia",
  "dataEvento": "2026-12-24T20:00:00"
}
```

Entrar em grupo:

```json
{
  "codigoUnico": "AB12-CD34"
}
```

Resposta de grupo:

```json
{
  "id": 1,
  "nome": "Amigo secreto da familia",
  "codigoUnico": "AB12-CD34",
  "dono": {
    "id": 1,
    "nome": "Kayky",
    "email": "kayky@example.com"
  },
  "membros": [],
  "dataCriacao": "2026-05-14T00:00:00",
  "dataSorteio": null,
  "dataEvento": "2026-12-24T20:00:00"
}
```

### Sorteio

```http
POST /api/groups/{groupId}/draw
GET /api/groups/{groupId}/draw/me
```

Resposta ao sortear:

```json
{
  "groupId": 1,
  "participantCount": 3,
  "performedAt": "2026-05-14T00:00:00"
}
```

Resposta de `draw/me`:

```json
{
  "grupoId": 1,
  "amigoSecreto": {
    "id": 2,
    "nome": "Maria",
    "email": "maria@example.com"
  },
  "wishlist": {
    "id": 10,
    "usuario": {
      "id": 2,
      "nome": "Maria",
      "email": "maria@example.com"
    },
    "sugestaoIa": null,
    "itens": []
  }
}
```

### Wishlist

```http
GET /api/wishlist
POST /api/wishlist/items
PUT /api/wishlist/items/{itemId}
DELETE /api/wishlist/items/{itemId}
GET /api/groups/{groupId}/users/{ownerId}/wishlist
POST /api/groups/{groupId}/users/{ownerId}/wishlist/ai-suggestion
```

Criar/editar item:

```json
{
  "nomeProduto": "Fone bluetooth",
  "link": "https://example.com/fone"
}
```

Resposta de wishlist:

```json
{
  "id": 10,
  "usuario": {
    "id": 1,
    "nome": "Kayky",
    "email": "kayky@example.com"
  },
  "sugestaoIa": null,
  "itens": [
    {
      "id": 100,
      "nomeProduto": "Fone bluetooth",
      "link": "https://example.com/fone"
    }
  ]
}
```

Resposta de IA:

```json
{
  "wishlistId": 10,
  "suggestion": "Sugestao em texto simples"
}
```

### Mensagens

```http
POST /api/groups/{groupId}/messages
GET /api/groups/{groupId}/messages/chats
GET /api/groups/{groupId}/messages/{otherUserId}
PATCH /api/groups/{groupId}/messages/{otherUserId}/read
GET /api/messages/unread-count
```

Enviar mensagem:

```json
{
  "destinatarioId": 2,
  "conteudo": "Voce prefere camiseta preta ou azul?"
}
```

Resposta de mensagem para remetente:

```json
{
  "id": 50,
  "grupoId": 1,
  "remetente": {
    "id": 1,
    "nome": "Kayky",
    "email": "kayky@example.com"
  },
  "destinatario": {
    "id": 2,
    "nome": "Maria",
    "email": "maria@example.com"
  },
  "nomeRemetenteExibicao": "Kayky",
  "conteudo": "Voce prefere camiseta preta ou azul?",
  "dataEnvio": "2026-05-14T00:00:00",
  "lida": false,
  "anonima": true
}
```

Para destinatario:

```json
{
  "remetente": null,
  "nomeRemetenteExibicao": "amigo secreto"
}
```

Resumo de chats:

```json
[
  {
    "grupoId": 1,
    "outroUsuarioId": 2,
    "nomeExibicao": "Maria",
    "anonimoParaUsuario": false,
    "unreadCount": 0
  },
  {
    "grupoId": 1,
    "outroUsuarioId": 3,
    "nomeExibicao": "amigo secreto",
    "anonimoParaUsuario": true,
    "unreadCount": 1
  }
]
```

Contador:

```json
{
  "unreadCount": 3
}
```

### Notificacoes

```http
GET /api/notifications
GET /api/notifications/unread-count
PATCH /api/notifications/{notificationId}/read
PATCH /api/notifications/read-all
DELETE /api/notifications/{notificationId}
DELETE /api/notifications
```

Resposta:

```json
[
  {
    "id": 1,
    "usuario": {
      "id": 1,
      "nome": "Kayky",
      "email": "kayky@example.com"
    },
    "titulo": "Sorteio realizado",
    "mensagem": "Voce tirou Maria no amigo secreto.",
    "dataCriacao": "2026-05-14T00:00:00",
    "lida": false
  }
]
```

## WebSocket/STOMP

Endpoints:

```text
/ws
/ws-sockjs
```

No `CONNECT`, enviar um dos headers nativos:

```text
Authorization: Bearer <jwt>
```

ou:

```text
access_token: <jwt>
```

Assinaturas privadas:

```text
/user/queue/messages
/user/queue/unread-count
```

Payload de nova mensagem:

```json
{
  "groupId": 1,
  "messageId": 50,
  "senderDisplayName": "amigo secreto",
  "content": "Mensagem",
  "sentAt": "2026-05-14T00:00:00",
  "unreadCount": 1
}
```

Payload de contador:

```json
{
  "unreadCount": 1
}
```

## Regras de Seguranca

- Nao expor `oauthId`.
- Nao expor pareamento completo do sorteio.
- Nao permitir wishlist de terceiros fora da regra do sorteio.
- Nao permitir mensagens fora dos pares do sorteio.
- Nao permitir alteracao de notificacoes de outro usuario.
- Nao permitir entrada/saida/remocao apos sorteio.
- Nao permitir endpoints dev em producao.
- Nao permitir Swagger em producao.
- Nao permitir origem frontend sem HTTPS em producao.

## Testes

Comando:

```powershell
.\mvnw.cmd test
```

A suite cobre:

- regras de grupo;
- sorteio;
- wishlist;
- IA e limite de uso;
- email;
- mensagens;
- notificacoes;
- CORS;
- cookie/logout;
- contrato de erros;
- validações de produção;
- endpoints de apoio ao frontend.

