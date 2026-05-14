# Secret Wish Backend

Backend Spring Boot do Secret Wish.

## Requisitos

- Java 21
- MySQL 8+
- Maven Wrapper incluso no projeto (`mvnw.cmd`)
- Google OAuth2 configurado no Google Cloud Console
- Gmail com senha de app para SMTP, se `MAIL_ENABLED=true`

## Execucao local

1. Crie o banco local:

```sql
CREATE DATABASE secret_wish;
```

2. Copie o arquivo de exemplo:

```powershell
Copy-Item src\main\resources\application-local.example.properties src\main\resources\application-local.properties
```

3. Preencha `src/main/resources/application-local.properties` com seus valores locais.

4. Suba a aplicacao com o profile local:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

5. Acesse:

- API: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui.html`
- Login Google: `http://localhost:8080/oauth2/authorization/google`

## Variaveis locais principais

`application-local.properties` aceita estas chaves:

```properties
DB_URL=jdbc:mysql://localhost:3306/secret_wish
DB_USERNAME=root
DB_PASSWORD=1234

JWT_SECRET=replace-with-a-long-local-secret
DEV_AUTH_ENABLED=true
AUTH_COOKIE_SECURE=false
AUTH_COOKIE_SAME_SITE=Lax
FRONTEND_ORIGIN=http://localhost:3000
SWAGGER_ENABLED=true

GOOGLE_CLIENT_ID=replace-with-google-client-id
GOOGLE_CLIENT_SECRET=replace-with-google-client-secret
GOOGLE_REDIRECT_URI={baseUrl}/login/oauth2/code/{registrationId}

MAIL_ENABLED=false
MAIL_USERNAME=your-app-gmail@gmail.com
MAIL_PASSWORD=your-gmail-app-password

AI_ENABLED=false
OPENAI_API_KEY=local-placeholder-openai-key
```

## Endpoints de desenvolvimento

Os endpoints `/api/dev/**` existem apenas quando:

```properties
DEV_AUTH_ENABLED=true
```

Use somente em ambiente local. Em producao eles ficam desabilitados pelo profile `prod`.

## Swagger

O Swagger fica habilitado quando:

```properties
SWAGGER_ENABLED=true
```

Em producao, `application-prod.properties` desabilita:

```properties
springdoc.api-docs.enabled=false
springdoc.swagger-ui.enabled=false
```

## Mapa da API

Todos os endpoints abaixo exigem JWT, exceto OAuth2, Swagger quando habilitado e `/api/dev/**` quando `DEV_AUTH_ENABLED=true`.

## Contrato de erro

Todas as respostas de erro da API usam o mesmo formato:

```json
{
  "timestamp": "2026-05-13T14:30:00",
  "status": 400,
  "error": "Erro de validacao",
  "message": "Existem campos invalidos na requisicao",
  "fields": {
    "nome": "must not be blank"
  }
}
```

`fields` sempre existe. Quando o erro nao for de validacao de campos, ele vem como objeto vazio.

### Autenticacao

- `GET /oauth2/authorization/google`: inicia login com Google.
- `GET /login/oauth2/code/google`: callback chamado pelo Google.
- `GET /api/auth/status`: retorna se a sessao atual possui JWT valido.
- `POST /api/logout`: remove o cookie HTTP-only de autenticacao.
- `GET /api/me`: retorna o usuario autenticado.

### Grupos

- `GET /api/groups`: lista grupos do usuario.
- `POST /api/groups`: cria grupo.
- `POST /api/groups/join`: entra em grupo pelo codigo unico.
- `DELETE /api/groups/{groupId}/members/{memberId}`: remove membro.
- `DELETE /api/groups/{groupId}/leave`: sai do grupo antes do sorteio.
- `DELETE /api/groups/{groupId}`: exclui grupo.

### Wishlist

- `GET /api/wishlist`: retorna ou cria a wishlist do usuario.
- `POST /api/wishlist/items`: adiciona item.
- `PUT /api/wishlist/items/{itemId}`: atualiza item.
- `DELETE /api/wishlist/items/{itemId}`: remove item.
- `GET /api/groups/{groupId}/users/{ownerId}/wishlist`: consulta wishlist visivel.
- `POST /api/groups/{groupId}/users/{ownerId}/wishlist/ai-suggestion`: gera sugestao por IA.

### Sorteio

- `POST /api/groups/{groupId}/draw`: executa ou refaz o sorteio.
- `GET /api/groups/{groupId}/draw/me`: consulta o amigo secreto do usuario autenticado.

### Mensagens

- `POST /api/groups/{groupId}/messages`: envia mensagem privada.
- `GET /api/groups/{groupId}/messages/{otherUserId}`: lista conversa.
- `PATCH /api/groups/{groupId}/messages/{otherUserId}/read`: marca conversa como lida.
- `GET /api/messages/unread-count`: conta mensagens nao lidas.

### Notificacoes

- `GET /api/notifications`: lista notificacoes.
- `GET /api/notifications/unread-count`: conta notificacoes nao lidas.
- `PATCH /api/notifications/{notificationId}/read`: marca uma notificacao como lida.
- `PATCH /api/notifications/read-all`: marca todas como lidas.
- `DELETE /api/notifications/{notificationId}`: exclui uma notificacao.
- `DELETE /api/notifications`: exclui todas.

### Desenvolvimento local

- `GET /api/dev`: ajuda dos endpoints locais.
- `GET /api/dev/token`: gera JWT local pelo browser.
- `POST /api/dev/token`: gera JWT local.
- `GET /api/dev/email/test?to=email@example.com`: envia email de teste pelo browser.
- `POST /api/dev/email/test?to=email@example.com`: envia email de teste.

## WebSocket

Endpoints STOMP:

- `/ws`: conexao WebSocket nativa.
- `/ws-sockjs`: fallback SockJS.

No `CONNECT`, envie o JWT em um destes headers nativos:

```text
Authorization: Bearer seu-jwt
```

ou:

```text
access_token: seu-jwt
```

Destinos para assinar:

- `/user/queue/messages`: novas mensagens privadas.
- `/user/queue/unread-count`: contador atualizado de mensagens nao lidas.

## OAuth2 Google

No Google Cloud Console, configure o redirect URI:

```text
http://localhost:8080/login/oauth2/code/google
```

Em producao, configure tambem o dominio real:

```text
https://seu-dominio.com/login/oauth2/code/google
```

## SMTP Gmail

Para enviar emails:

```properties
MAIL_ENABLED=true
MAIL_USERNAME=seu-email@gmail.com
MAIL_PASSWORD=sua-senha-de-app
```

Use senha de app do Google, nao a senha normal da conta.

## Producao

Suba com o profile `prod`:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=prod"
```

Variaveis obrigatorias em producao:

```properties
DB_URL=
DB_USERNAME=
DB_PASSWORD=
JWT_SECRET=
FRONTEND_ORIGIN=
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
```

Variaveis opcionais em producao:

```properties
GOOGLE_REDIRECT_URI={baseUrl}/login/oauth2/code/{registrationId}
MAIL_ENABLED=false
MAIL_USERNAME=
MAIL_PASSWORD=
AI_ENABLED=false
OPENAI_API_KEY=
```

Defaults seguros do profile `prod`:

- `app.dev-auth.enabled=false`
- `app.auth.cookie-secure=true`
- `app.auth.cookie-same-site=Lax`
- `spring.jpa.hibernate.ddl-auto=validate`
- `spring.flyway.enabled=true`
- Swagger desabilitado

Se frontend e backend ficarem em dominios diferentes, use:

```properties
AUTH_COOKIE_SAME_SITE=None
AUTH_COOKIE_SECURE=true
```

O backend bloqueia a inicializacao em `prod` quando encontra configuracao insegura, como Swagger habilitado, dev auth habilitado, `JWT_SECRET` curto ou `FRONTEND_ORIGIN` sem HTTPS.

## Testes

```powershell
.\mvnw.cmd test
```

## Observacoes de seguranca

- Nunca versionar `application-local.properties`.
- Nao usar secrets locais em producao.
- Rotacionar secrets se forem compartilhados por engano.
- Manter `JWT_SECRET` longo e aleatorio.
