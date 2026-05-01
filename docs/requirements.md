# ✅ Requisitos Funcionais (RF)

## 👤 Gestão de Usuários

* RF01 – Permitir cadastro de usuários.
* RF02 – Permitir login e logout.
* RF03 – Permitir edição dos dados do usuário.
* RF04 – Permitir exclusão de conta.
* RF05 – Garantir unicidade de email.

## 👥 Gestão de Grupos

* RF06 – Permitir criação de grupos de amigo secreto.
* RF07 – Gerar código de convite (5 caracteres).
* RF08 – Permitir entrada via link ou código.
* RF09 – Impedir entrada duplicada no grupo.
* RF10 – Permitir definir tema, valor mínimo e valor máximo (opcional).
* RF11 – Permitir que apenas o líder inicie o sorteio.
* RF12 – Impedir início do sorteio com número ímpar de participantes.
* RF13 – Definir status do grupo (Aberto, Sorteado, Finalizado).

## 🎲 Sorteio

* RF14 – Realizar sorteio automático entre participantes.
* RF15 – Garantir que ninguém tire a si mesmo.
* RF16 – Garantir que cada participante tenha exatamente 1 pessoa para presentear e 1 pessoa que o presenteia.
* RF17 – Garantir que o usuário veja apenas quem ele tirou.
* RF18 – Garantir que o usuário não veja quem o tirou.
* RF19 – Notificar participantes após sorteio.
* RF20 – Bloquear alterações no grupo após sorteio (opcional).

## 🔁 Controle de Re-sorteio

* RF21 – Permitir re-sorteio apenas pelo líder.
* RF22 – Solicitar confirmação antes do re-sorteio.
* RF23 – Registrar histórico de sorteios.
* RF24 – Notificar participantes após re-sorteio.

## 🛍️ Lista de Desejos

* RF25 – Permitir cadastro de até 10 itens por usuário.
* RF26 – Permitir adicionar nome do produto, valor e link de compra.
* RF27 – Permitir editar e excluir itens.
* RF28 – Permitir visualização da lista pela pessoa que tirou o usuário.
* RF29 – Permitir edição da lista após sorteio.
* RF30 – Validar faixa de preço quando definida no grupo.

## 💬 Chat Anônimo

* RF31 – Criar chat entre usuário e a pessoa que ele tirou.
* RF32 – Criar chat entre usuário e a pessoa que o tirou.
* RF33 – No chat com quem o usuário tirou, exibir identidade apenas para quem envia.
* RF34 – No chat com quem tirou o usuário, ocultar identidade do remetente.
* RF35 – Permitir envio de mensagens.
* RF36 – Permitir visualização do histórico de mensagens.
* RF37 – Manter chats separados mesmo quando forem as mesmas pessoas.

## 📧 Notificações

* RF38 – Enviar email com resultado do sorteio.
* RF39 – Notificar ao receber mensagem.
* RF40 – Permitir ativar ou desativar notificações.
* RF41 – Notificar entrada em grupo.

---

# ⚙️ Requisitos Não Funcionais (RNF)

## 🔐 Segurança

* RNF01 – Garantir anonimato no chat.
* RNF02 – Criptografar senhas (bcrypt).
* RNF03 – Proteger contra SQL Injection, XSS e CSRF.
* RNF04 – Implementar autenticação segura (JWT).
* RNF05 – Não expor quem tirou o usuário em nenhuma API.
* RNF06 – Ocultar metadados no chat.

## ⚡ Desempenho

* RNF07 – Realizar sorteio em até 2 segundos.
* RNF08 – Garantir baixa latência no chat.

## 🌐 Usabilidade

* RNF09 – Interface intuitiva.
* RNF10 – Sistema responsivo.

## 📧 Confiabilidade

* RNF11 – Garantir envio de emails (≥95%).
* RNF12 – Processamento assíncrono de emails.

## 🛡️ Integridade

* RNF13 – Garantir uma associação por participante.
* RNF14 – Evitar inconsistências no sorteio.
* RNF15 – Evitar duplicidade de chats.

## 🔄 Disponibilidade

* RNF16 – Disponibilidade mínima de 99%.

## 🧱 Manutenibilidade

* RNF17 – Código modular e organizado.
* RNF18 – Separação em camadas (Controller, Service, Repository).
