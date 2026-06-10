package com.classmarket.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "avaliacoes")
@Data
@NoArgsConstructor
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** "site" ou "produto" */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoAvaliacao tipo;

    @Column(nullable = false)
    private Integer nota;

    @Column(columnDefinition = "TEXT")
    private String comentario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id")
    private Produto produto;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    public enum TipoAvaliacao { site, produto }
}
