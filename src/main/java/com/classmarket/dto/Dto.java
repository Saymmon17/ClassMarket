package com.classmarket.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

// ── Auth ──────────────────────────────────────────────────────────────────

public class Dto {

    @Data
    public static class LoginRequest {
        @NotBlank @Email
        private String email;
        @NotBlank
        private String senha;
    }

    @Data
    public static class LoginResponse {
        private String token;
        private UsuarioResumo usuario;
    }

    @Data
    public static class CadastroRequest {
        @NotBlank @Size(min = 2, max = 120)
        private String nome;
        @NotBlank @Email
        private String email;
        @Size(max = 20)
        private String telefone;
        @Size(max = 80)
        private String curso;
        @NotBlank @Size(min = 4, max = 72)
        private String senha;
    }

    // ── Usuário ───────────────────────────────────────────────────────────

    @Data
    public static class UsuarioResumo {
        private Integer id;
        private String nome;
        private String email;
        private String telefone;
        private String curso;
        private boolean adm;
    }

    // ── Produto ───────────────────────────────────────────────────────────

    @Data
    public static class ProdutoRequest {
        @NotBlank @Size(max = 120)
        private String nome;
        private String descricao;
        @NotNull @DecimalMin("0.01")
        private BigDecimal preco;
        @NotNull @Min(0)
        private Integer estoque;
        @NotNull
        private Integer categoriaId;
        @NotBlank @Size(max = 10)
        private String bloco;
        @NotBlank @Size(max = 10)
        private String sala;
        private String fotoUrl;
    }

    @Data
    public static class ProdutoResponse {
        private Integer id;
        private String nome;
        private String descricao;
        private BigDecimal preco;
        private Integer estoque;
        private String categoria;
        private Integer categoriaId;
        private String bloco;
        private String sala;
        private String fotoUrl;
        private String status;
        private VendedorResumo vendedor;
        private Double mediaAvaliacoes;
        private Integer totalAvaliacoes;
    }

    @Data
    public static class VendedorResumo {
        private Integer id;
        private String nome;
        private String telefone;
    }

    // ── Avaliação ─────────────────────────────────────────────────────────

    @Data
    public static class AvaliacaoRequest {
        @NotBlank
        private String tipo; // "site" ou "produto"
        @NotNull @Min(1) @Max(5)
        private Integer nota;
        private String comentario;
        private Integer produtoId; // obrigatório se tipo = "produto"
    }

    @Data
    public static class AvaliacaoResponse {
        private Integer id;
        private String tipo;
        private Integer nota;
        private String comentario;
        private String usuarioNome;
        private String produtoNome;
        private String criadoEm;
    }

    // ── Reset de senha ────────────────────────────────────────────────────

    @Data
    public static class EsqueciSenhaRequest {
        @NotBlank @Email
        private String email;
    }

    @Data
    public static class RedefinirSenhaRequest {
        @NotBlank
        private String token;
        @NotBlank @Size(min = 4)
        private String novaSenha;
    }

    @Data
    public static class RedefinirSenhaEmailRequest {
        @NotBlank @Email
        private String email;
        @NotBlank @Size(min = 4)
        private String novaSenha;
    }

    // ── Status ADM ────────────────────────────────────────────────────────

    @Data
    public static class StatusRequest {
        @NotBlank
        private String status; // "aprovado" ou "negado"
    }

    // ── Notificação ───────────────────────────────────────────────────────

    @Data
    public static class NotificacaoResponse {
        private Integer id;
        private String titulo;
        private String mensagem;
        private Boolean lida;
        private String criadoEm;
    }

    // ── Deletar avaliação com motivo ──────────────────────────────────────

    @Data
    public static class DeletarAvaliacaoRequest {
        @NotBlank
        private String motivo;
    }

    // ── Resposta genérica ─────────────────────────────────────────────────

    @Data
    public static class MensagemResponse {
        private String mensagem;
        public MensagemResponse(String mensagem) { this.mensagem = mensagem; }
    }
}
