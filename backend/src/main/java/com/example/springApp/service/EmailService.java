package com.example.springApp.service;

import com.example.springApp.exception.BusinessException;
import com.example.springApp.model.Draw;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final boolean mailEnabled;
    private final String fromAddress;

    public EmailService(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${app.mail.enabled:false}") boolean mailEnabled,
            @Value("${spring.mail.username:}") String fromAddress
    ) {
        this.mailSender = mailSenderProvider.getIfAvailable();
        this.mailEnabled = mailEnabled;
        this.fromAddress = fromAddress;
    }

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

    public void sendDrawResults(List<DrawResultEmail> results) {
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
            LOGGER.error("Falha inesperada no envio dos emails de sorteio", ex);
        }
    }

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

    private void sendDrawResult(DrawResultEmail result) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (fromAddress != null && !fromAddress.isBlank()) {
                message.setFrom(fromAddress);
            }
            message.setTo(result.recipientEmail());
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

    private String buildDrawResultBody(DrawResultEmail result) {
        return """
                Ola, %s!

                O sorteio do grupo "%s" foi realizado.

                Voce tirou: %s

                A wishlist deve ser consultada pelo sistema.
                """.formatted(
                result.recipientName(),
                result.groupName(),
                result.secretFriendName()
        );
    }

    private String normalizeRecipient(String recipientEmail) {
        if (recipientEmail == null || recipientEmail.isBlank()) {
            throw new BusinessException("Email de destino deve ser informado");
        }

        String normalized = recipientEmail.trim();
        if (!normalized.contains("@") || normalized.startsWith("@") || normalized.endsWith("@")) {
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
