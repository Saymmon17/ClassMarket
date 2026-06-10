package com.classmarket.controller;

import com.classmarket.dto.Dto.*;
import com.classmarket.service.AvaliacaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/avaliacoes")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    // ── GET /api/avaliacoes ───────────────────────────────────────────────
    @GetMapping
    public List<AvaliacaoResponse> listar() {
        return avaliacaoService.listarTodas();
    }

    // ── GET /api/avaliacoes/produto/{produtoId} ───────────────────────────
    @GetMapping("/produto/{produtoId}")
    public List<AvaliacaoResponse> listarPorProduto(@PathVariable Integer produtoId) {
        return avaliacaoService.listarPorProduto(produtoId);
    }

    // ── POST /api/avaliacoes ──────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<AvaliacaoResponse> salvar(
            @Valid @RequestBody AvaliacaoRequest req,
            @AuthenticationPrincipal String email) {
        return ResponseEntity.status(201)
                .body(avaliacaoService.salvar(req, email));
    }
}
