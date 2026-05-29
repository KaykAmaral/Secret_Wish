# 🎁 Secret Wish: The Intelligent Secret Santa Platform

<div align="center">
  <p align="center">
    <img src="./frontend/src/assets/logoprojeto.png" alt="Secret Wish Logo" width="600">
  </p>
  <p align="center">
    <strong>Elevando a tradição do Amigo Secreto ao próximo nível com IA generativa, sincronização em tempo real e arquitetura de segurança de ponta.</strong>
  </p>

  <!-- Status & Badges -->
  <p align="center">
    <img src="https://img.shields.io/badge/Status-Project_Stable-success?style=for-the-badge" alt="Status">
    <img src="https://img.shields.io/badge/Architecture-Clean_Layers-blue?style=for-the-badge" alt="Architecture">
    <img src="https://img.shields.io/badge/Frontend-React_19-61DAFB?style=for-the-badge&logo=react" alt="React">
    <img src="https://img.shields.io/badge/Backend-Spring_Boot_3.5-6DB33F?style=for-the-badge&logo=springboot" alt="Spring Boot">
    <img src="https://img.shields.io/badge/AI-Llama_3.3_via_Groq-8A2BE2?style=for-the-badge" alt="AI">
  </p>
</div>

---

## 📖 Sobre o Projeto

O **Secret Wish** é uma solução Fullstack completa para organização de Amigo Secreto. Diferente de ferramentas simples, ele foca na **experiência do usuário** e na **resolução de atritos**:
- **O dilema do presente:** Resolvido por IA que analisa a wishlist e sugere itens compatíveis.
- **A quebra de segredo:** Evitada por chats anônimos onde o sorteador pode tirar dúvidas sem se revelar.
- **A falha do sorteio:** Impedida por um algoritmo de cadeia circular que garante que ninguém fique de fora ou tire a si mesmo.

---

## 🛠️ Stack Tecnológica de Elite

### 💻 Backend (Robustez & Segurança)
*   **Java 21 (LTS):** Utilizando as últimas features de performance e linguagem.
*   **Spring Boot 3.5:** Ecossistema principal para serviços REST e WebSockets.
*   **Spring Security + OAuth2:** Fluxo de autenticação híbrido (Local + Google).
*   **JWT + Cookies HttpOnly:** Sessões seguras protegidas contra ataques XSS e CSRF.
*   **Spring Data JPA + Hibernate:** Camada de persistência resiliente com suporte a múltiplos dialetos.
*   **Spring AI:** Integração agnóstica com provedores de IA (configurado com Groq Cloud).
*   **Flyway:** Controle de versão rigoroso do esquema de banco de dados.

### 🎨 Frontend (Experiência do Usuário)
*   **React 19:** O estado da arte em bibliotecas de interface.
*   **Vite 6:** Build tool ultra-rápida.
*   **Framer Motion:** Animações baseadas em física para uma UI "viva".
*   **Lucide React:** Iconografia moderna e consistente.
*   **StompJS & SockJS:** Comunicação real-time via WebSockets para chats e notificações.
*   **Vanilla CSS + Glassmorphism:** Design proprietário focado em profundidade e transparência.

---

## 📐 Engenharia de Software & Arquitetura

O sistema foi desenhado seguindo princípios de **Clean Architecture** e **S.O.L.I.D**, garantindo baixo acoplamento e alta testabilidade.

### 🏗️ Arquitetura de Macro-Nível
```mermaid
graph TD
    subgraph "Camada de Cliente"
        A[React SPA] -- WebSocket/STOMP --> B[Real-time Bridge]
        A -- REST/JSON --> C[API Gateway / Security]
    end

    subgraph "Ecossistema Backend"
        C --> D[Security Filter Chain]
        D --> E[REST Controllers]
        E --> F[Service Layer]
        F --> G[Spring AI Engine]
        F --> H[Draw Algorithm]
        F --> I[Notification Engine]
    end

    subgraph "Recursos Externos"
        G --> J[Groq/Llama 3 LLM]
        C --> K[Google OAuth2]
    end

    subgraph "Persistência"
        F --> L[JPA Repository]
        L --> M[(MySQL / H2 Database)]
    end
```

### 🎲 Algoritmo de Sorteio Circular (Circular Chain Draw)
Diferente de sorteios aleatórios simples, nossa lógica garante um ciclo hamiltoniano fechado.

```mermaid
flowchart LR
    A[Lista de Membros] --> B{Embaralhamento}
    B --> C[Criar Cadeia]
    C --> D[Pessoa 1 -> Pessoa 2]
    D --> E[Pessoa 2 -> Pessoa 3]
    E --> F[Pessoa N -> Pessoa 1]
    F --> G[Persistir Pares]
    G --> H[Notificar via Email/WS]
```

### 🔐 Fluxo de Segurança & JWT
Implementamos o padrão de "Stateless Auth with Stateful Control" usando cookies.

```mermaid
sequenceDiagram
    participant C as Browser (React)
    participant S as Server (Spring Boot)
    participant DB as Database
    
    C->>S: POST /api/auth/login
    S->>DB: Valida Credenciais
    DB-->>S: OK
    S->>S: Gera JWT assinado
    S-->>C: Set-Cookie: jwt (HttpOnly, Secure)
    Note over C, S: Subsequentes requisições enviam o cookie automaticamente
    C->>S: GET /api/groups
    S->>S: JWT Filter extrai e valida ID do usuário
    S-->>C: Dados Autorizados
```

---

## 🌟 Funcionalidades Principais (Deep Dive)

### 🤖 Inteligência Artificial Sugestiva
O sistema não apenas lista desejos, ele os interpreta. Se você adicionou "Cafeteira" e "Moedor", a IA (Llama 3 via Groq) pode sugerir ao seu amigo secreto marcas específicas de grãos gourmet ou filtros especiais, baseando-se no perfil de itens.

### 💬 Chat Híbrido: Revelado vs Anônimo
O backend gerencia o "contexto de visão" de cada mensagem:
1.  **Doador para Recebedor:** O doador vê o nome real do recebedor.
2.  **Recebedor para Doador:** O recebedor vê o remetente como "Seu Amigo Secreto".
3.  **Filtro de Integridade:** O backend bloqueia mensagens caso o par não exista no sorteio atual.

---

## 📁 Estrutura do Projeto

```text
.
├── backend/               # Nucleo de Inteligencia & API
│   ├── src/main/java      # Domínio, Serviços, Segurança e Controllers
│   ├── src/main/resources # Configurações, SQL Migrations e Templates de Email
│   └── data/              # Storage local para desenvolvimento
├── frontend/              # Interface & Interação
│   ├── src/api            # Centralização de Axios e Interceptores
│   ├── src/services       # Camada de integração (REST & WebSockets)
│   ├── src/pages          # Componentes de tela e lógica de UI
│   └── src/components     # Elementos reutilizáveis de Design System
└── docs/                  # Documentação técnica e especificações
```

---

## 🚀 Guia de Início Rápido

### Pré-requisitos
- **Java 21**
- **Node.js 18+**
- **Maven** (Opcional, mvnw incluso)

### Execução Unificada (Local)

1.  **Backend:**
    ```bash
    cd backend
    ./mvnw spring-boot:run
    ```
    *O banco H2 será criado automaticamente em `./backend/data/`.*

2.  **Frontend:**
    ```bash
    cd frontend
    npm install
    npm run dev
    ```

3.  **Acesse:** `http://localhost:5173`

---

## 🛡️ Segurança de Produção

O projeto conta com o `ProductionSafetyValidator`, que impede que a aplicação suba em perfil de produção (`prod`) caso:
- O Segredo JWT seja o padrão ou muito curto.
- A flag `app.dev-auth.enabled` esteja ativa.
- O Banco de dados esteja configurado sem senha.
- As origens de CORS contenham wildcards (`*`).

---

## 👤 Integrantes do GRUPO

**David** - *Idealização e Desenvolvimento Frontend*
- [GitHub](https://github.com/d4vid-dev)

**Beatriz** - * Designer e Desenvolvimento Frontend*
- [GitHub](https://github.com/BeatrizYashodara)

**Martha** - *Desenvolvimento Backend e Documentação*
- [GitHub](https://github.com/Martha-coda)

**Kayk** - *Idealização e Desenvolvimento Backend*
- [GitHub](https://github.com/KaykAmaral)

---
<div align="center">
  <p>Construído com a robustez do ecossistema Java e a agilidade do React.</p>
  <p>© 2026 Secret Wish Project.</p>
</div>
