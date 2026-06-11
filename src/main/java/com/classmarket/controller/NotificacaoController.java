package com.classmarket.controller;

import com.classmarket.dto.Dto.*;
import com.classmarket.service.NotificacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificacoes")
public class NotificacaoController {

    private final NotificacaoService notifService;

    public NotificacaoController(NotificacaoService notifService) {
        this.notifService = notifService;
    }

    // ── GET /api/notificacoes ─────────────────────────────────────────────
    @GetMapping
    public List<NotificacaoResponse> minhas(@AuthenticationPrincipal String email) {
        return notifService.listarMinhas(email);
    }

    // ── GET /api/notificacoes/nao-lidas ───────────────────────────────────
    @GetMapping("/nao-lidas")
    public ResponseEntity<Map<String, Long>> naoLidas(@AuthenticationPrincipal String email) {
        return ResponseEntity.ok(Map.of("total", notifService.contarNaoLidas(email)));
    }

    // ── PATCH /api/notificacoes/ler-todas ─────────────────────────────────
    @PatchMapping("/ler-todas")
    public ResponseEntity<Void> lerTodas(@AuthenticationPrincipal String email) {
        notifService.marcarTodasComoLidas(email);
        return ResponseEntity.noContent().build();
    }
}
