package com.classmarket.controller;

import com.classmarket.dto.Dto.*;
import com.classmarket.security.JwtUtil;
import com.classmarket.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

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

    // ── POST /api/produtos/upload-foto ────────────────────────────────────
    // Recebe a imagem como multipart/form-data e devolve a string Base64
    // pronta para ser usada como fotoUrl no cadastro do produto.
    // Isso evita trafegar Base64 gigante direto no JSON do POST /api/produtos.
    @PostMapping("/upload-foto")
    public ResponseEntity<Map<String, String>> uploadFoto(
            @RequestParam("foto") MultipartFile foto) throws IOException {

        if (foto.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Arquivo vazio."));
        }

        long maxBytes = 10 * 1024 * 1024; // 10 MB
        if (foto.getSize() > maxBytes) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Imagem muito grande. Máximo 10 MB."));
        }

        String mimeType = foto.getContentType() != null
                ? foto.getContentType() : "image/jpeg";
        String base64 = Base64.getEncoder().encodeToString(foto.getBytes());
        String dataUrl = "data:" + mimeType + ";base64," + base64;

        return ResponseEntity.ok(Map.of("fotoUrl", dataUrl));
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
