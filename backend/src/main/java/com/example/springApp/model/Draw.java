package com.example.springApp.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "draws")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Draw {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group grupo;

    @ManyToOne
    @JoinColumn(name = "giver_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User destinatario;

}