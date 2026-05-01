# Requisitos do Sistema - Amigo Secreto Inteligente

## 🎯 Objetivo
Uma aplicação Fullstack para sorteios de amigo secreto com sugestões de presentes baseadas em IA e chat de comunicação anônima.

## ✅ Requisitos Funcionais (RF)
*   **RF01 - Cadastro de Usuários:** O sistema deve permitir login/cadastro com e-mail e senha.
*   **RF02 - Gestão de Grupos:** Usuários podem criar grupos e convidar participantes via código único.
*   **RF03 - Lista de Desejos:** Cada usuário pode cadastrar até **10 itens** (Nome e Link).
*   **RF04 - Sorteio Automático:** O sistema deve realizar o sorteio aleatório, garantindo que ninguém tire a si mesmo.
*   **RF05 - Chat Identificado (Sorteado):** Chat direto com a pessoa que você tirou (identidade revelada).
*   **RF06 - Chat Anônimo (Doador):** Chat com quem tirou você (identidade oculta como "Amigo Secreto").
*   **RF07 - Sugestões de IA:** O sistema deve sugerir novos itens para a lista com base no que o usuário já adicionou.

## ⚙️ Requisitos Não-Funcionais (RNF)
*   **RNF01 - Segurança:** Senhas criptografadas e autenticação via JWT.
*   **RNF02 - Privacidade:** O banco de dados deve proteger a relação do sorteio até o momento da revelação.
*   **RNF03 - Tempo Real:** As mensagens dos chats devem ser entregues via WebSockets.
*   **RNF04 - Usabilidade:** Interface responsiva para uso em dispositivos móveis.