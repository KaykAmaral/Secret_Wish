package com.example.springApp.controller;

import com.example.springApp.dto.TestEmailResponse;
import com.example.springApp.service.EmailService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dev/email")
@ConditionalOnProperty(prefix = "app.dev-auth", name = "enabled", havingValue = "true")
public class DevEmailController {

    private final EmailService emailService;

    public DevEmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/test")
    public TestEmailResponse testFromBrowser(@RequestParam String to) {
        return sendTestEmail(to);
    }

    @PostMapping("/test")
    public TestEmailResponse test(@RequestParam String to) {
        return sendTestEmail(to);
    }

    private TestEmailResponse sendTestEmail(String recipientEmail) {
        emailService.sendTestEmail(recipientEmail);
        return new TestEmailResponse(recipientEmail.trim(), "Email de teste enviado");
    }
}
