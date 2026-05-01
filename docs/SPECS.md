# 📌 SPECS.md — Amigo Secreto Inteligente

## 📖 Visão Geral

Sistema web para gerenciamento de amigo secreto com:

* sorteio automático
* lista de desejos
* chat anônimo em tempo real
* notificações por email

Arquitetura baseada em frontend + backend separados.

---

# 🧱 Arquitetura

## 📌 Tecnologias

* Frontend: React
* Backend: Spring Boot
* Banco: MySQL
* Autenticação: JWT + Google OAuth
* Comunicação:

  * REST (operações gerais)
  * WebSocket (chat em tempo real)

## 🏗️ Diagrama de Arquitetura

Este diagrama representa a estrutura geral do sistema,
incluindo frontend, backend, banco de dados e serviços auxiliares.

```mermaid
flowchart LR

    U[Usuário] --> FE[Frontend - React]

    FE -->|HTTP REST| API[Backend - Spring Boot]
    FE -->|WebSocket| WS[WebSocket Server]

    WS --> API

    API --> C[Controller]
    C --> S[Service]
    S --> R[Repository]

    R --> DB[(Banco de Dados)]

    S --> Q[Fila de Emails]
    Q --> W[Worker]
    W --> SMTP[Servidor SMTP]
```

---

# 🔐 Autenticação e Segurança

## 📌 Métodos de Login

* Email + senha
* Google OAuth

## 📌 Segurança

* Senhas criptografadas (bcrypt)
* Autenticação via JWT
* Controle de acesso por grupo
* Dados sensíveis nunca expostos na API

## ⚠️ Regra importante

* Identidade no chat é ocultada apenas na interface
* Backend controla o que pode ou não ser exibido

---

# 🗄️ Modelagem de Dados

## 📊 Diagrama Entidade-Relacionamento

Este diagrama apresenta as entidades principais do sistema
e seus relacionamentos.

```mermaid
erDiagram

    USER {
        int id
        string nome
        string email
        string senha
        string provider
    }

    GROUP {
        int id
        string nome
        string codigo_convite
        string status
        float valor_min
        float valor_max
    }

    GROUP_PARTICIPANT {
        int id
        int user_id
        int group_id
    }

    DRAW {
        int id
        int group_id
        int giver_id
        int receiver_id
    }

    DRAW_HISTORY {
        int id
        int group_id
        datetime data
    }

    WISHLIST_ITEM {
        int id
        int user_id
        string nome
        float preco
        string link
    }

    CHAT {
        int id
        int group_id
        int user1_id
        int user2_id
    }

    MESSAGE {
        int id
        int chat_id
        int sender_id
        string conteudo
        datetime timestamp
    }

    USER ||--o{ GROUP_PARTICIPANT : participa
    GROUP ||--o{ GROUP_PARTICIPANT : possui

    GROUP ||--o{ DRAW : tem
    USER ||--o{ DRAW : envia
    USER ||--o{ DRAW : recebe

    GROUP ||--o{ DRAW_HISTORY : historico

    USER ||--o{ WISHLIST_ITEM : possui

    CHAT ||--o{ MESSAGE : contem
```

## 👤 User

* id
* nome
* email (único)
* senha
* provider (LOCAL / GOOGLE)

## 👥 Group

* id
* nome
* codigo_convite
* lider_id
* status (ABERTO, SORTEADO, FINALIZADO)
* valor_min (opcional)
* valor_max (opcional)

## 🤝 GroupParticipant

* id
* user_id
* group_id

## 🎲 Draw

* id
* group_id
* giver_id
* receiver_id

## 🔁 DrawHistory

* id
* group_id
* data

## 🎁 WishlistItem

* id
* user_id
* nome
* preco
* link

## 💬 Chat

* id
* group_id
* user1_id
* user2_id

## 💬 Message

* id
* chat_id
* sender_id
* conteudo
* timestamp

---

# 🎲 Lógica do Sorteio

## 🔄 Fluxo do Sorteio

Este diagrama mostra o processo de execução do sorteio,
garantindo que as regras de negócio sejam respeitadas.

```mermaid
flowchart TD

    A[Iniciar Sorteio] --> B{Número de participantes é par?}

    B -- Não --> C[Erro: não pode sortear]
    B -- Sim --> D[Listar participantes]

    D --> E[Embaralhar lista]

    E --> F[Gerar pares circulares]

    F --> G{Alguém tirou a si mesmo?}

    G -- Sim --> E
    G -- Não --> H[Salvar resultado]

    H --> I[Notificar participantes]

    I --> J[Fim]
```

## 📌 Regras

* Ninguém pode tirar a si mesmo
* Cada participante tem:

  * 1 pessoa que presenteia
  * 1 pessoa que o presenteia
* Resultado é sigiloso

## 📌 Algoritmo

1. Listar participantes
2. Embaralhar lista
3. Associar:

   * i → i+1
   * último → primeiro

## 🔁 Re-sorteio

* Apenas o líder pode executar
* Requer confirmação
* Histórico deve ser salvo

---

# 💬 Chat em Tempo Real

## 🔄 Fluxo do Chat

Este diagrama representa o envio e recebimento de mensagens
em tempo real, respeitando as regras de anonimato.

```mermaid
flowchart TD

    A[Usuário envia mensagem] --> B{Tipo de chat}

    B -->|Quem ele tirou| C[Identidade visível]
    B -->|Quem tirou ele| D[Identidade oculta]

    C --> E[Salvar mensagem]
    D --> E

    E --> F[Enviar via WebSocket]

    F --> G[Usuário recebe em tempo real]
```

## 📌 Tecnologia

* WebSocket (Spring + STOMP)

## 📌 Estrutura

Cada usuário possui:

* chat com quem ele tirou
* chat com quem tirou ele

## 📌 Regras

* Identidade pode ser ocultada dependendo do contexto
* Mensagens persistidas no banco
* Histórico acessível

---

# 📧 Sistema de Email

## 📌 Funcionalidades

* Enviar resultado do sorteio
* Notificar novas mensagens
* Notificar eventos (entrada em grupo)

## 📌 Implementação

* SMTP (Gmail ou similar)
* Envio assíncrono

---

# 🎨 Frontend

## 📌 Telas

* Login / Cadastro
* Dashboard
* Grupos
* Lista de desejos
* Chat
* Perfil

## 📌 Fluxo principal

1. Usuário autentica
2. Cria ou entra em grupo
3. Aguarda sorteio
4. Recebe resultado
5. Interage via lista e chat

---

# 📡 API (visão geral)

## 🔐 Auth

* POST /auth/register
* POST /auth/login
* POST /auth/google

## 👤 User

* GET /users/me
* PUT /users/me
* DELETE /users/me

## 👥 Groups

* POST /groups
* GET /groups
* POST /groups/join
* POST /groups/{id}/draw
* POST /groups/{id}/redraw

## 🎁 Wishlist

* GET /wishlist
* POST /wishlist
* PUT /wishlist/{id}
* DELETE /wishlist/{id}

## 💬 Chat

* GET /chats
* GET /chats/{id}/messages

## 💬 WebSocket

* /ws/chat
* /topic/messages
* /app/send

---

# 🚀 Deploy

## 📌 Sugestão

* Frontend: Vercel
* Backend: Render / Railway
* Banco: MySQL cloud

---

# 📊 Considerações Técnicas

## ⚠️ Pontos Críticos

* Autenticação (JWT + OAuth)
* Sincronização do chat em tempo real
* Controle de acesso por grupo
* Segurança das APIs

## ✅ Estratégia de Entrega

1. Autenticação
2. Grupos + sorteio
3. Lista de desejos
4. Email
5. Chat em tempo real

---

# 🧩 Boas Práticas

* Commits pequenos e frequentes
* Branch por feature
* Integração contínua entre frontend e backend
* Testes a cada funcionalidade entregue
* Evitar adicionar novas features durante o desenvolvimento
