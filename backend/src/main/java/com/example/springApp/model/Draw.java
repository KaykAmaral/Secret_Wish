package com.example.springApp.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "draws",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_draw_group_giver", columnNames = {"group_id", "giver_id"}),
                @UniqueConstraint(name = "uk_draw_group_receiver", columnNames = {"group_id", "receiver_id"})
        }
)
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
    private User remetente;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User destinatario;

}
