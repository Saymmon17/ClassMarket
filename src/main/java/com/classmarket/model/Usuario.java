package com.classmarket.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 20)
    private String telefone;

    @Column(length = 80)
    private String curso;

    @Column(nullable = false, length = 255)
    private String senha;

    @Column(name = "reset_token", length = 64)
    private String resetToken;

    @Column(name = "reset_expira")
    private LocalDateTime resetExpira;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();
}
