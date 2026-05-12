package com.example.springApp.dto;

public record TestEmailResponse(
        String recipientEmail,
        String message
) {
}
