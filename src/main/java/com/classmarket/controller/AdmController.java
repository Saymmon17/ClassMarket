package com.classmarket.controller;

import com.classmarket.dto.Dto.*;
import com.classmarket.service.ProdutoService;
import com.classmarket.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/adm")
@PreAuthorize("hasRole('ADM')")
public class AdmController {

    private final ProdutoService produtoService;
    private final UsuarioService usuarioService;

    public AdmController(ProdutoService produtoService, UsuarioService usuarioService) {
        this.produtoService = produtoService;
        this.usuarioService = usuarioService;
    }

    // ── Produtos ──────────────────────────────────────────────────────────

    @GetMapping("/produtos")
    public List<ProdutoResponse> listarTodosProdutos() {
        return produtoService.listarTodosAdm();
    }

    @GetMapping("/produtos/pendentes")
    public List<ProdutoResponse> listarPendentes() {
        return produtoService.listarPendentes();
    }

    @PatchMapping("/produtos/{id}/status")
    public ProdutoResponse mudarStatus(
            @PathVariable Integer id,
            @RequestBody StatusRequest req) {
        return produtoService.mudarStatus(id, req.getStatus());
    }

    @DeleteMapping("/produtos/{id}")
    public ResponseEntity<Void> excluirProduto(@PathVariable Integer id) {
        produtoService.excluir(id, null, true);
        return ResponseEntity.noContent().build();
    }

    // ── Usuários ──────────────────────────────────────────────────────────

    @GetMapping("/usuarios")
    public List<UsuarioResumo> listarUsuarios() {
        return usuarioService.listar();
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Void> desativarUsuario(@PathVariable Integer id) {
        usuarioService.desativar(id);
        return ResponseEntity.noContent().build();
    }
}
