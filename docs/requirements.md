# \# Secret Wish - Requisitos

# 

# \## Visao Geral

# 

# Secret Wish e um backend para organizar grupos de amigo secreto com login Google, sorteio em cadeia, wishlist, mensagens anonimas privadas, notificacoes em tempo real, email de resultado e sugestoes por IA baseadas na wishlist.

# 

# \## Requisitos Funcionais

# 

# \### Autenticacao e Sessao

# 

# \- O sistema deve autenticar usuarios usando OAuth2 com Google.

# \- O sistema deve aceitar apenas contas Google com email verificado.

# \- O backend deve emitir JWT apos login bem-sucedido.

# \- O JWT deve ser enviado ao frontend por cookie HTTP-only.

# \- O usuario deve conseguir consultar o status da sessao atual.

# \- O usuario deve conseguir consultar seus dados autenticados.

# \- O usuario deve conseguir encerrar a sessao removendo o cookie de autenticacao.

# \- Em ambiente local, quando habilitado, o sistema pode gerar tokens por endpoints de desenvolvimento.

# 

# \### Usuarios

# 

# \- O sistema deve criar automaticamente um usuario no primeiro login Google.

# \- O sistema deve atualizar nome, email e identificador Google em logins posteriores.

# \- A API publica nao deve expor `oauthId` ou dados internos do provedor OAuth.

# 

# \### Grupos

# 

# \- O usuario autenticado deve conseguir criar um grupo.

# \- Cada usuario pode ser dono de apenas um grupo.

# \- O dono do grupo tambem deve ser membro do grupo.

# \- O backend deve gerar um codigo unico no formato `XXXX-XXXX` para entrada no grupo.

# \- Um usuario deve conseguir entrar em um grupo usando o codigo unico.

# \- Um usuario pode participar de varios grupos.

# \- Um usuario deve conseguir listar os grupos em que participa.

# \- Um usuario deve conseguir consultar um grupo por ID apenas se participar dele.

# \- O dono deve conseguir remover participantes antes do sorteio.

# \- Participantes devem conseguir sair do grupo apenas antes do sorteio.

# \- O dono nao deve sair do proprio grupo; deve excluir o grupo.

# \- O dono deve conseguir excluir o grupo e seus dados relacionados.

# 

# \### Sorteio

# 

# \- Apenas o dono do grupo pode realizar o sorteio.

# \- O sorteio deve exigir no minimo 3 participantes.

# \- O sorteio deve formar uma cadeia circular, por exemplo `A -> B -> C -> A`.

# \- Nenhum participante pode tirar a si mesmo.

# \- Cada participante deve tirar exatamente uma pessoa.

# \- Cada participante deve ser tirado exatamente uma vez.

# \- O dono pode refazer o sorteio quantas vezes quiser.

# \- Ao refazer o sorteio, mensagens antigas do grupo devem ser apagadas.

# \- Ao realizar o sorteio, o sistema deve registrar a data do sorteio.

# \- Ao realizar o sorteio, o sistema deve criar notificacoes para os participantes.

# \- Ao realizar o sorteio, o sistema deve enviar email com o resultado, quando email estiver habilitado.

# \- A API de sorteio nao deve expor o pareamento completo para o frontend.

# \- Cada usuario deve conseguir consultar apenas quem ele tirou.

# 

# \### Wishlist

# 

# \- O usuario deve conseguir consultar sua propria wishlist.

# \- Se o usuario ainda nao tiver wishlist, o sistema deve criar uma automaticamente.

# \- O usuario deve conseguir adicionar itens na propria wishlist.

# \- O usuario deve conseguir editar itens da propria wishlist.

# \- O usuario deve conseguir remover itens da propria wishlist.

# \- Cada item de wishlist deve conter nome e link.

# \- O usuario deve conseguir visualizar a wishlist da pessoa que tirou no sorteio.

# \- Um usuario nao deve conseguir visualizar wishlist de outra pessoa fora da regra do sorteio.

# 

# \### Sugestoes com IA

# 

# \- O sistema deve gerar sugestoes de presente usando IA quando a funcionalidade estiver habilitada.

# \- As sugestoes devem ser baseadas exclusivamente nos itens existentes na wishlist.

# \- A IA nao deve sugerir produtos fora da wishlist.

# \- A sugestao deve ser texto simples.

# \- A wishlist deve ter pelo menos um item para gerar sugestao.

# \- Cada usuario pode gerar no maximo 3 sugestoes por hora.

# \- Quando o limite for atingido, a API deve responder `429 Too Many Requests`.

# \- Se a IA estiver desabilitada, a API deve retornar erro de regra de negocio sem quebrar a aplicacao.

# \- Se o cliente de IA falhar, a API deve retornar erro controlado.

# 

# \### Mensagens

# 

# \- O sistema deve permitir mensagens privadas entre remetente e destinatario.

# \- Nao deve existir chat de grupo.

# \- Mensagens so podem ser trocadas entre pares relacionados pelo sorteio.

# \- O remetente real deve ficar oculto para o destinatario.

# \- Para o destinatario, o remetente deve aparecer como `amigo secreto`.

# \- Para quem enviou a mensagem, o proprio nome pode aparecer normalmente.

# \- O usuario deve conseguir consultar conversas permitidas.

# \- O usuario deve conseguir listar os chats permitidos dentro de um grupo.

# \- O resumo de chats deve informar contador de mensagens nao lidas por conversa.

# \- O usuario deve conseguir marcar uma conversa como lida.

# \- O usuario deve conseguir consultar o total de mensagens nao lidas.

# 

# \### Notificacoes

# 

# \- O sistema deve criar notificacoes quando o sorteio for realizado.

# \- O usuario deve conseguir listar suas notificacoes.

# \- O usuario deve conseguir consultar o total de notificacoes nao lidas.

# \- O usuario deve conseguir marcar uma notificacao como lida.

# \- O usuario deve conseguir marcar todas as notificacoes como lidas.

# \- O usuario deve conseguir excluir uma notificacao.

# \- O usuario deve conseguir excluir todas as suas notificacoes.

# \- Um usuario nao deve acessar ou alterar notificacoes de outro usuario.

# 

# \### WebSocket

# 

# \- O sistema deve oferecer conexao WebSocket/STOMP.

# \- O STOMP deve exigir JWT no `CONNECT`.

# \- O usuario deve conseguir assinar fila privada de novas mensagens.

# \- O usuario deve conseguir assinar fila privada de contador de mensagens nao lidas.

# \- O sistema deve enviar notificacao em tempo real quando uma mensagem nova for persistida.

# \- O sistema deve enviar contador atualizado de mensagens nao lidas.

# 

# \### Email

# 

# \- O sistema deve enviar email com o resultado do sorteio quando email estiver habilitado.

# \- O email deve informar apenas quem o usuario tirou.

# \- O email nao deve incluir a wishlist.

# \- A wishlist deve ser consultada pelo sistema.

# \- Falha no envio de um email nao deve impedir o envio dos demais.

# \- Falha de email nao deve desfazer o sorteio ja persistido.

# \- Em ambiente local, quando habilitado, deve existir endpoint de teste de email.

# 

# \### Documentacao e Desenvolvimento

# 

# \- O sistema deve expor Swagger quando habilitado.

# \- O Swagger deve ficar desabilitado em producao.

# \- Endpoints `/api/dev/\*\*` devem existir apenas quando `DEV\_AUTH\_ENABLED=true`.

# \- Endpoints de desenvolvimento devem ficar desabilitados em producao.

# \- A API deve usar contrato padronizado para erros.

# 

# \## Requisitos Nao Funcionais

# 

# \### Seguranca

# 

# \- Em producao, o sistema deve bloquear inicializacao com configuracao insegura.

# \- Em producao, `DEV\_AUTH\_ENABLED` deve ser falso.

# \- Em producao, Swagger deve estar desabilitado.

# \- Em producao, cookies de autenticacao devem usar `Secure=true`.

# \- Cookies de autenticacao devem ser `HttpOnly`.

# \- `SameSite=None` so pode ser usado com cookie seguro.

# \- `JWT\_SECRET` deve ter pelo menos 32 caracteres em producao.

# \- Origens de frontend em producao devem usar HTTPS.

# \- CORS nao deve aceitar `\*` em producao.

# \- Dados sensiveis nao devem ser versionados.

# \- Logs nao devem registrar JWT, senhas, secrets, API keys ou headers de autorizacao.

# \- Respostas genericas de erro interno nao devem expor stacktrace.

# 

# \### Confiabilidade

# 

# \- Operacoes criticas devem usar transacoes.

# \- Notificacoes em tempo real e emails devem ser disparados apenas apos persistencia bem-sucedida quando aplicavel.

# \- Falhas de WebSocket nao devem quebrar fluxo principal de mensagem.

# \- Falhas de email nao devem quebrar sorteio.

# \- Regras de negocio devem retornar erros controlados.

# 

# \### Persistencia e Migracoes

# 

# \- O banco de producao deve usar MySQL.

# \- O schema deve ser controlado por Flyway.

# \- Em producao, Hibernate deve usar `ddl-auto=validate`.

# \- Migrations devem criar constraints para garantir unicidade de dono de grupo, codigo de grupo e pareamentos de sorteio.

# 

# \### Observabilidade

# 

# \- Erros inesperados devem ser logados no backend.

# \- Mensagens de erro para clientes devem ser padronizadas.

# \- Logs devem evitar dados sensiveis.

# 

# \### Compatibilidade com Frontend

# 

# \- O backend deve permitir CORS para origens configuradas.

# \- O backend deve suportar cookies com credenciais.

# \- O backend deve disponibilizar endpoints de status de sessao.

# \- O backend deve disponibilizar payloads estaveis para grupos, wishlist, sorteio, mensagens, notificacoes e contadores.

# \- O backend deve suportar deploy atras de proxy HTTPS.

# 

# \### Testabilidade

# 

# \- Regras principais devem ter testes automatizados.

# \- Contratos de erro devem ter testes.

# \- Configuracoes inseguras de producao devem ter testes.

# \- Fluxos principais devem poder ser testados manualmente via Swagger/browser quando localmente habilitado.

# 

# 

