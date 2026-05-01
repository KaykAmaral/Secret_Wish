# Protocolo de Agentes e Automação

## 🤖 Agente de IA (Gift Advisor)
O agente de IA atua como um assistente de compras pessoal.
- **Gatilho:** Quando o usuário possui entre 1 e 9 itens e solicita uma sugestão.
- **Lógica:** A IA recebe o array de nomes de produtos atuais e deve retornar 3 objetos contendo `{nome, justificativa_da_escolha}`.
- **Restrição:** Não sugerir produtos que o usuário já possui na lista.

## 🎭 Agente de Anonimato (Chat Masking)
Regras para o funcionamento do chat duplo:
1.  **Contexto "Eu tirei alguém":** O usuário logado envia mensagem para o `receiver_id`. O frontend exibe o nome real do presenteado.
2.  **Contexto "Alguém me tirou":** O usuário logado recebe mensagens. O backend deve interceptar o objeto `sender` e substituir o nome real por "Amigo Secreto" antes de enviar o JSON para o frontend.
3.  **Proteção de Dados:** O ID real de quem tirou o usuário nunca deve ser enviado no payload do WebSocket para o destinatário (prevenção de inspeção via Console).

## 🎲 Agente de Sorteio (The Dealer)
- **Lógica:** Algoritmo de Shuffle (Fisher-Yates) aplicado à lista de participantes do grupo.
- **Validação:** Garante que `giver_id != receiver_id` em 100% dos casos.