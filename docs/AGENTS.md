# 🤖 AGENTS.md — Amigo Secreto Inteligente

## 📖 Visão Geral

A IA será utilizada como funcionalidade complementar para melhorar a experiência do usuário, sem impactar o funcionamento principal do sistema.

---

# 🎯 Objetivo do Agente

👉 Sugerir ideias de presentes com base na lista de desejos do usuário.

---

# 🧠 Tecnologia Utilizada

- Modelo de IA: Google Gemini
- Integração via API
- Consumo realizado pelo backend (Spring Boot)

---

# ⚙️ Arquitetura de Integração

## 📌 Fluxo Geral

Frontend (React)

   ↓
   
Backend (Spring Boot)

   ↓
   
Serviço de IA (Google Gemini API)

---

## 📌 Descrição do Fluxo

1. O usuário solicita sugestões de presentes na interface
2. O frontend envia a lista de desejos para o backend
3. O backend:
   - valida os dados
   - formata o prompt
   - envia requisição para a API do Gemini
4. O Gemini processa e retorna sugestões
5. O backend trata a resposta
6. O backend envia as sugestões para o frontend
7. O frontend exibe os resultados ao usuário

---

## 📌 Estrutura da Requisição

Frontend → Backend
POST /ai/suggestions

Backend → Gemini API
- HTTP POST
- Payload com prompt estruturado

---

## 📌 Estrutura de Resposta

Gemini API → Backend
- Texto com sugestões

Backend → Frontend
- JSON estruturado com lista de sugestões

---

## 📌 Tratamento de Erros

- Timeout da API externa
- Falha de conexão
- Resposta inválida

Fallback:
- Retornar mensagem padrão
- Não bloquear o fluxo do sistema

---

## 📌 Segurança

- API Key armazenada no backend (não exposta no frontend)
- Sanitização dos dados enviados ao modelo
- Limitação de requisições (rate limit)

---

## 📌 Performance

- Chamadas assíncronas
- Possível cache de respostas (opcional)
- Timeout configurado para evitar travamentos
