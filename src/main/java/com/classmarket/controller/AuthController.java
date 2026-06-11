package com.classmarket.controller;

import com.classmarket.dto.Dto.*;
import com.classmarket.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/cadastro")
    public ResponseEntity<UsuarioResumo> cadastrar(@Valid @RequestBody CadastroRequest req) {
        return ResponseEntity.status(201).body(authService.cadastrar(req));
    }

    @PostMapping("/esqueci-senha")
    public ResponseEntity<MensagemResponse> esqueciSenha(
            @Valid @RequestBody EsqueciSenhaRequest req) {
        String token = authService.gerarTokenReset(req.getEmail());
        // Em produção, enviar por e-mail. Aqui retornamos o token para demo.
        return ResponseEntity.ok(new MensagemResponse(
                "Token gerado. Em produção seria enviado por e-mail. Token (dev): " + token));
    }

    @GetMapping("/token-valido")
    public ResponseEntity<MensagemResponse> tokenValido(@RequestParam String token) {
        boolean valido = authService.tokenValido(token);
        return ResponseEntity.ok(new MensagemResponse(valido ? "valido" : "invalido"));
    }

    @PostMapping("/redefinir-senha")
    public ResponseEntity<MensagemResponse> redefinirSenha(
            @Valid @RequestBody RedefinirSenhaRequest req) {
        authService.redefinirSenha(req.getToken(), req.getNovaSenha());
        return ResponseEntity.ok(new MensagemResponse("Senha redefinida com sucesso"));
    }

    // ── Verificar e-mail (para o fluxo de esqueci senha via código) ────────
    @PostMapping("/verificar-email")
    public ResponseEntity<?> verificarEmail(
            @Valid @RequestBody EsqueciSenhaRequest req) {
        try {
            return ResponseEntity.ok(authService.verificarEmail(req.getEmail()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(new MensagemResponse(e.getMessage()));
        }
    }

    // ── Redefinir senha por e-mail (fluxo via código EmailJS) ────────────
    @PostMapping("/redefinir-senha-email")
    public ResponseEntity<MensagemResponse> redefinirSenhaPorEmail(
            @Valid @RequestBody RedefinirSenhaEmailRequest req) {
        authService.redefinirSenhaPorEmail(req.getEmail(), req.getNovaSenha());
        return ResponseEntity.ok(new MensagemResponse("Senha redefinida com sucesso"));
    }
}