package com.example.springApp.service;

import com.example.springApp.exception.BusinessException;
import com.example.springApp.model.Draw;
import com.example.springApp.model.Group;
import com.example.springApp.model.User;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailServiceTest {

    @Test
    void toDrawResultEmailsCreatesSnapshotsWithoutLazyEntityDependency() {
        EmailService service = new EmailService(provider(null), false, "");
        Group group = Group.builder().id(10L).nome("Grupo").build();
        User giver = user(1L, "Ana");
        User receiver = user(2L, "Bruno");
        Draw draw = Draw.builder().grupo(group).remetente(giver).destinatario(receiver).build();

        List<EmailService.DrawResultEmail> snapshots = service.toDrawResultEmails(List.of(draw));

        assertThat(snapshots).containsExactly(new EmailService.DrawResultEmail(
                10L,
                "Grupo",
                1L,
                "Ana",
                "ana@example.com",
                "Bruno"
        ));
    }

    @Test
    void sendDrawResultsDoesNothingWhenMailIsDisabled() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        EmailService service = new EmailService(provider(mailSender), false, "from@example.com");

        service.sendDrawResults(List.of(drawEmail()));

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendDrawResultsSendsEachEmailWhenEnabled() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        EmailService service = new EmailService(provider(mailSender), true, "from@example.com");

        service.sendDrawResults(List.of(drawEmail()));

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage message = captor.getValue();
        assertThat(message.getFrom()).isEqualTo("from@example.com");
        assertThat(message.getTo()).containsExactly("ana@example.com");
        assertThat(message.getSubject()).isEqualTo("Resultado do amigo secreto - Grupo");
        assertThat(message.getText())
                .contains("O sorteio do grupo \"Grupo\" aconteceu!")
                .contains("Voce tirou Bruno")
                .contains("Va conferir a lista de desejos");
    }

    @Test
    void sendDrawResultsSkipsInvalidRecipientWithoutCallingMailSender() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        EmailService service = new EmailService(provider(mailSender), true, "from@example.com");

        service.sendDrawResults(List.of(new EmailService.DrawResultEmail(
                10L,
                "Grupo",
                1L,
                "Ana",
                "email-invalido",
                "Bruno"
        )));

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendDrawResultsKeepsSendingWhenOneRecipientFails() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        EmailService service = new EmailService(provider(mailSender), true, "from@example.com");
        org.mockito.Mockito.doThrow(new RuntimeException("SMTP offline"))
                .doNothing()
                .when(mailSender)
                .send(any(SimpleMailMessage.class));

        service.sendDrawResults(List.of(
                drawEmail(),
                new EmailService.DrawResultEmail(10L, "Grupo", 2L, "Carla", "carla@example.com", "Ana")
        ));

        verify(mailSender, times(2)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendTestEmailValidatesConfigurationAndRecipient() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        EmailService disabled = new EmailService(provider(mailSender), false, "from@example.com");
        EmailService enabled = new EmailService(provider(mailSender), true, "from@example.com");

        assertThatThrownBy(() -> disabled.sendTestEmail("user@example.com"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("desabilitado");
        assertThatThrownBy(() -> enabled.sendTestEmail("email-invalido"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("invalido");

        assertThatCode(() -> enabled.sendTestEmail(" user@example.com "))
                .doesNotThrowAnyException();

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getTo()).containsExactly("user@example.com");
    }

    @Test
    void sendWelcomeEmailSendsFriendlyMessageWhenEnabled() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        EmailService service = new EmailService(provider(mailSender), true, "from@example.com");

        service.sendWelcomeEmail(" Ana ", " ana@example.com ");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage message = captor.getValue();
        assertThat(message.getFrom()).isEqualTo("from@example.com");
        assertThat(message.getTo()).containsExactly("ana@example.com");
        assertThat(message.getSubject()).isEqualTo("Bem-vindo ao Secret Wish");
        assertThat(message.getText())
                .contains("Ola, Ana!")
                .contains("Sua conta foi criada com sucesso")
                .contains("montar sua lista de desejos");
    }

    @Test
    void sendWelcomeEmailDoesNotFailWhenMailIsDisabledOrRecipientIsInvalid() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        EmailService disabled = new EmailService(provider(mailSender), false, "from@example.com");
        EmailService enabled = new EmailService(provider(mailSender), true, "from@example.com");

        disabled.sendWelcomeEmail("Ana", "ana@example.com");
        enabled.sendWelcomeEmail("Ana", "email-invalido");

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendTestEmailWrapsMailSenderFailuresAsBusinessException() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        EmailService service = new EmailService(provider(mailSender), true, "from@example.com");
        org.mockito.Mockito.doThrow(new RuntimeException("SMTP offline"))
                .when(mailSender)
                .send(any(SimpleMailMessage.class));

        assertThatThrownBy(() -> service.sendTestEmail("user@example.com"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Nao foi possivel enviar");
    }

    private EmailService.DrawResultEmail drawEmail() {
        return new EmailService.DrawResultEmail(
                10L,
                "Grupo",
                1L,
                "Ana",
                "ana@example.com",
                "Bruno"
        );
    }

    private User user(Long id, String name) {
        return User.builder()
                .id(id)
                .nome(name)
                .email(name.toLowerCase() + "@example.com")
                .build();
    }

    private ObjectProvider<JavaMailSender> provider(JavaMailSender mailSender) {
        @SuppressWarnings("unchecked")
        ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(mailSender);
        return provider;
    }
}
