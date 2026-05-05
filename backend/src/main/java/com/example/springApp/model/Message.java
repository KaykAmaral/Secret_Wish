package com.example.springApp.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group grupo;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User remetente;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User destinatario;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String conteudo;

    @Column(name = "data_envio")
    private LocalDateTime dataEnvio;

    @Column(name = "lida")
    private boolean lida = false;

    @Column(name = "anonima")
    private boolean anonima = true;

    @PrePersist
    void prePersist() {
        if (dataEnvio == null) {
            dataEnvio = LocalDateTime.now();
        }
    }

}
