package com.example.springApp.model;

import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "groups_table")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    // Codigo curto usado pelo frontend para convite/entrada no grupo.
    @Column(name = "codigo_unico", nullable = false, unique = true, length = 9)
    private String codigoUnico;

    // Regra de negocio: cada usuario pode ser dono de apenas um grupo.
    @ManyToOne
    @JoinColumn(name = "dono_id", nullable = false, unique = true)
    private User dono;

    @ManyToMany
    @JoinTable(
            name = "group_members",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_group_member",
                    columnNames = {"group_id", "user_id"}
            )
    )
    @Builder.Default
    private Set<User> membros = new HashSet<>();

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;

    @Column(name = "data_sorteio")
    private LocalDateTime dataSorteio;

    @Column(name = "data_evento")
    private LocalDateTime dataEvento;

    @PrePersist
    void prePersist() {
        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }
    }

}
