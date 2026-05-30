package com.example.springApp.service;

import com.example.springApp.exception.BusinessException;
import com.example.springApp.model.Draw;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);
    // Validacao propositalmente simples: evita chamadas SMTP obvias com destino invalido sem tentar substituir validacao RFC completa.
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final JavaMailSender mailSender;
    private final boolean mailEnabled;
    private final String fromAddress;

    public EmailService(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${app.mail.enabled:false}") boolean mailEnabled,
            @Value("${spring.mail.username:}") String fromAddress
    ) {
        // ObjectProvider permite subir a aplicacao mesmo quando o starter de email nao criou um JavaMailSender utilizavel.
        this.mailSender = mailSenderProvider.getIfAvailable();
        this.mailEnabled = mailEnabled;
        this.fromAddress = fromAddress;
    }

    /**
     * Cria snapshots imutaveis do resultado para envio apos o fechamento da transacao.
     */
    public List<DrawResultEmail> toDrawResultEmails(List<Draw> draws) {
        return draws.stream()
                // Snapshot evita depender de entidades lazy depois que a transacao termina.
                .map(draw -> new DrawResultEmail(
                        draw.getGrupo().getId(),
                        draw.getGrupo().getNome(),
                        draw.getRemetente().getId(),
                        draw.getRemetente().getNome(),
                        draw.getRemetente().getEmail(),
                        draw.getDestinatario().getNome()
                ))
                .toList();
    }

    /**
     * Envia todos os resultados disponiveis sem derrubar a aplicacao se o provedor falhar.
     */
    @Async("mailTaskExecutor")
    public void sendDrawResults(List<DrawResultEmail> results) {
        // O metodo roda assincrono para que SMTP lento nao aumente a latencia da requisicao de sorteio.
        if (results == null || results.isEmpty()) {
            return;
        }

        try {
            if (!mailEnabled) {
                LOGGER.info("Envio de email desabilitado. {} resultado(s) de sorteio nao foram enviados.", results.size());
                return;
            }

            if (mailSender == null) {
                LOGGER.error("Envio de email habilitado, mas JavaMailSender nao esta configurado.");
                return;
            }

            for (DrawResultEmail result : results) {
                // Uma falha individual nao deve impedir os demais participantes de receberem email.
                sendDrawResult(result);
            }
        } catch (Exception ex) {
            // Ultima barreira de protecao: email nunca deve derrubar a regra principal do sorteio.
            LOGGER.error("Falha inesperada no envio dos emails de sorteio", ex);
        }
    }

    /**
     * Envia uma mensagem amigavel quando a conta e criada, sem bloquear o fluxo de cadastro.
     */
    @Async("mailTaskExecutor")
    public void sendWelcomeEmail(String recipientName, String recipientEmail) {
        // Boas-vindas e uma notificacao auxiliar; cadastro deve continuar mesmo se email estiver desligado.
        if (!mailEnabled) {
            LOGGER.info("Envio de email desabilitado. Boas-vindas para {} nao foram enviadas.", recipientEmail);
            return;
        }

        if (mailSender == null) {
            LOGGER.error("Envio de email habilitado, mas JavaMailSender nao esta configurado.");
            return;
        }

        String normalizedRecipient;
        try {
            normalizedRecipient = normalizeRecipient(recipientEmail);
        } catch (BusinessException ex) {
            LOGGER.warn("Email de boas-vindas ignorado por destinatario invalido: {}", recipientEmail);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (fromAddress != null && !fromAddress.isBlank()) {
                message.setFrom(fromAddress);
            }
            message.setTo(normalizedRecipient);
            message.setSubject("Bem-vindo ao Secret Wish");
            message.setText(buildWelcomeBody(recipientName));

            mailSender.send(message);
        } catch (RuntimeException ex) {
            LOGGER.error("Falha ao enviar email de boas-vindas para {}", normalizedRecipient, ex);
        }
    }

    /**
     * Valida a configuracao SMTP enviando uma mensagem controlada para um destinatario informado.
     */
    public void sendTestEmail(String recipientEmail) {
        String normalizedRecipient = normalizeRecipient(recipientEmail);

        if (!mailEnabled) {
            throw new BusinessException("Envio de email esta desabilitado neste ambiente");
        }

        if (mailSender == null) {
            throw new BusinessException("JavaMailSender nao esta configurado");
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (fromAddress != null && !fromAddress.isBlank()) {
                message.setFrom(fromAddress);
            }
            message.setTo(normalizedRecipient);
            message.setSubject("Teste de email - Secret Wish");
            message.setText("""
                    Ola!

                    Este e um email de teste do Secret Wish.

                    Se voce recebeu esta mensagem, a configuracao SMTP esta funcionando.
                    """);

            mailSender.send(message);
        } catch (RuntimeException ex) {
            LOGGER.error("Falha ao enviar email de teste para {}", normalizedRecipient, ex);
            throw new BusinessException("Nao foi possivel enviar o email de teste");
        }
    }

    /**
     * Envia o resultado individual e isola falhas por participante.
     */
    private void sendDrawResult(DrawResultEmail result) {
        if (result == null) {
            LOGGER.warn("Resultado de sorteio sem dados foi ignorado no envio de email.");
            return;
        }

        String normalizedRecipient;
        try {
            // O email do usuario vem do banco, mas pode ter sido cadastrado antes de regras mais rigidas existirem.
            normalizedRecipient = normalizeRecipient(result.recipientEmail());
        } catch (BusinessException ex) {
            LOGGER.warn(
                    "Email de sorteio ignorado por destinatario invalido. Usuario: {}, grupo: {}",
                    result.recipientId(),
                    result.groupId()
            );
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (fromAddress != null && !fromAddress.isBlank()) {
                message.setFrom(fromAddress);
            }
            message.setTo(normalizedRecipient);
            message.setSubject("Resultado do amigo secreto - " + result.groupName());
            message.setText(buildDrawResultBody(result));

            mailSender.send(message);
        } catch (RuntimeException ex) {
            LOGGER.error(
                    "Falha ao enviar email do sorteio para usuario {} no grupo {}",
                    result.recipientId(),
                    result.groupId(),
                    ex
            );
        }
    }

    /**
     * Monta o corpo do email sem incluir dados sensiveis alem do par sorteado.
     */
    private String buildDrawResultBody(DrawResultEmail result) {
        // O corpo revela apenas o par sorteado do proprio usuario, sem expor o restante do grupo.
        return """
                Ola, %s!

                O sorteio do grupo "%s" aconteceu!

                Voce tirou %s no amigo secreto.

                Va conferir a lista de desejos dessa pessoa no Secret Wish para escolher um presente com carinho.
                """.formatted(
                result.recipientName(),
                result.groupName(),
                result.secretFriendName()
        );
    }

    /**
     * Mantem o texto de boas-vindas centralizado para cadastro local e OAuth2.
     */
    private String buildWelcomeBody(String recipientName) {
        // Nome ausente nao deve gerar saudacao quebrada em contas OAuth2 com perfil incompleto.
        String displayName = recipientName == null || recipientName.isBlank() ? "tudo bem" : recipientName.trim();
        return """
                Ola, %s!

                Seja bem-vindo ao Secret Wish.

                Sua conta foi criada com sucesso. Agora voce ja pode criar grupos, entrar em amigos secretos, montar sua lista de desejos e acompanhar tudo pelo app.

                Esperamos que seus sorteios sejam simples, divertidos e cheios de boas surpresas.
                """.formatted(displayName);
    }

    /**
     * Faz uma validacao leve para evitar chamadas SMTP com destinatario vazio ou claramente invalido.
     */
    private String normalizeRecipient(String recipientEmail) {
        if (recipientEmail == null || recipientEmail.isBlank()) {
            throw new BusinessException("Email de destino deve ser informado");
        }

        // Normaliza espacos de formulario/parametro sem alterar caixa, para preservar o email informado.
        String normalized = recipientEmail.trim();
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new BusinessException("Email de destino invalido");
        }
        return normalized;
    }

    public record DrawResultEmail(
            Long groupId,
            String groupName,
            Long recipientId,
            String recipientName,
            String recipientEmail,
            String secretFriendName
    ) {
    }
}
