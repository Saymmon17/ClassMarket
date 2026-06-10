package com.classmarket.controller;

import com.classmarket.dto.Dto.*;
import com.classmarket.security.JwtUtil;
import com.classmarket.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;
    private final JwtUtil        jwtUtil;

    public ProdutoController(ProdutoService produtoService, JwtUtil jwtUtil) {
        this.produtoService = produtoService;
        this.jwtUtil        = jwtUtil;
    }

    // ── GET /api/produtos?categoria=doces&busca=brow ──────────────────────
    @GetMapping
    public List<ProdutoResponse> listar(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String busca) {
        return produtoService.listarAprovados(categoria, busca);
    }

    // ── GET /api/produtos/{id} ────────────────────────────────────────────
    @GetMapping("/{id}")
    public ProdutoResponse buscarPorId(@PathVariable Integer id) {
        return produtoService.buscarPorId(id);
    }

    // ── GET /api/produtos/meus ────────────────────────────────────────────
    @GetMapping("/meus")
    public List<ProdutoResponse> meusProdutos(
            @AuthenticationPrincipal String email) {
        return produtoService.meusProdutos(email);
    }

    // ── POST /api/produtos ────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<ProdutoResponse> cadastrar(
            @Valid @RequestBody ProdutoRequest req,
            @AuthenticationPrincipal String email) {
        return ResponseEntity.status(201)
                .body(produtoService.cadastrar(req, email));
    }

    // ── PUT /api/produtos/{id} ────────────────────────────────────────────
    @PutMapping("/{id}")
    public ProdutoResponse atualizar(
            @PathVariable Integer id,
            @Valid @RequestBody ProdutoRequest req,
            @AuthenticationPrincipal String email,
            @RequestHeader("Authorization") String authHeader) {
        boolean adm = jwtUtil.isAdm(authHeader.substring(7));
        return produtoService.atualizar(id, req, email, adm);
    }

    // ── DELETE /api/produtos/{id} ─────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Integer id,
            @AuthenticationPrincipal String email,
            @RequestHeader("Authorization") String authHeader) {
        boolean adm = jwtUtil.isAdm(authHeader.substring(7));
        produtoService.excluir(id, email, adm);
        return ResponseEntity.noContent().build();
    }
}
