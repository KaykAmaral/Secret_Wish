# Especificações Técnicas (Technical Specs)

## 🛠 Stack Tecnológica
- **Backend:** Java 21 + Spring Boot 3.x.
- **Segurança:** Spring Security + JWT.
- **Banco de Dados:** MySQL (Relacional).
- **Comunicação:** Spring WebSockets (STOMP).
- **IA:** Integração com Gemini API / OpenAI API.
- **Frontend Sugerido:** React ou Angular (Consumindo a API REST).

## 🗄 Modelagem de Dados (Entidades)
1.  **User:** `id, nome, email, senha`.
2.  **Group:** `id, nome, codigo_convite, status (ABERTO/SORTEADO)`.
3.  **WishlistItem:** `id, nome, link, user_id`.
4.  **Match (Sorteio):** `id, group_id, giver_id, receiver_id`.
5.  **ChatMessage:** `id, match_id, sender_id, content, is_anonymous, timestamp`.

## 🚀 Principais Endpoints
- `POST /api/auth/register` - Cadastro de novos usuários.
- `POST /api/groups` - Criação de novo grupo de sorteio.
- `POST /api/groups/{id}/draw` - Execução da lógica de sorteio (Admin).
- `GET /api/wishlist/ai-suggestions` - Retorna 3 sugestões baseadas nos 10 itens atuais.
- `GET /api/chat/{matchId}/messages` - Recupera o histórico de chat.