package com.example.springApp.model;

import jakarta.persistence.*;


import java.util.List;

@Entity
@Table(name = "groups_table")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String codigoUnico;

    @ManyToOne
    private User dono;

    @ManyToMany
    private List<User> membros;
}