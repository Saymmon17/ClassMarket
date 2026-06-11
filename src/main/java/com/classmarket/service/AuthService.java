package com.classmarket.service;

import com.classmarket.dto.Dto.*;
import com.classmarket.model.Usuario;
import com.classmarket.repository.UsuarioRepository;
import com.classmarket.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private static final String ADM_EMAIL = "classmarket@proton.me";

    private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder   encoder;
    private final JwtUtil           jwtUtil;

    public AuthService(UsuarioRepository usuarioRepo,
                       PasswordEncoder encoder,
                       JwtUtil jwtUtil) {
        this.usuarioRepo = usuarioRepo;
        this.encoder     = encoder;
        this.jwtUtil     = jwtUtil;
    }

    // ── Login ──────────────────────────────────────────────────────────────
    public LoginResponse login(LoginRequest req) {
        Usuario u = usuarioRepo.findByEmailAndAtivoTrue(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Credenciais inválidas"));

        if (!encoder.matches(req.getSenha(), u.getSenha())) {
            throw new RuntimeException("Credenciais inválidas");
        }

        boolean adm = ADM_EMAIL.equals(u.getEmail());
        String token = jwtUtil.gerarToken(u.getEmail(), adm);

        LoginResponse resp = new LoginResponse();
        resp.setToken(token);
        resp.setUsuario(toResumo(u, adm));
        return resp;
    }

    // ── Cadastro ───────────────────────────────────────────────────────────
    @Transactional
    public UsuarioResumo cadastrar(CadastroRequest req) {
        if (usuarioRepo.existsByEmail(req.getEmail())) {
            throw new RuntimeException("E-mail já cadastrado");
        }

        Usuario u = new Usuario();
        u.setNome(req.getNome());
        u.setEmail(req.getEmail());
        u.setTelefone(req.getTelefone());
        u.setCurso(req.getCurso());
        u.setSenha(encoder.encode(req.getSenha()));
        usuarioRepo.save(u);

        return toResumo(u, false);
    }

    // ── Esqueci a senha ────────────────────────────────────────────────────
    @Transactional
    public String gerarTokenReset(String email) {
        Usuario u = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("E-mail não encontrado"));

        String token = UUID.randomUUID().toString().replace("-", "");
        u.setResetToken(token);
        u.setResetExpira(LocalDateTime.now().plusHours(1));
        usuarioRepo.save(u);

        // Em produção: enviar por e-mail (JavaMailSender)
        return token;
    }

    public boolean tokenValido(String token) {
        return usuarioRepo.findByResetToken(token)
                .map(u -> u.getResetExpira() != null
                          && u.getResetExpira().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    @Transactional
    public void redefinirSenha(String token, String novaSenha) {
        Usuario u = usuarioRepo.findByResetToken(token)
                .filter(usr -> usr.getResetExpira() != null
                               && usr.getResetExpira().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new RuntimeException("Token inválido ou expirado"));

        u.setSenha(encoder.encode(novaSenha));
        u.setResetToken(null);
        u.setResetExpira(null);
        usuarioRepo.save(u);
    }

    // ── Redefinir senha por e-mail (fluxo via código EmailJS) ────────────
    @Transactional
    public void redefinirSenhaPorEmail(String email, String novaSenha) {
        Usuario u = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("E-mail não encontrado"));

        u.setSenha(encoder.encode(novaSenha));
        // Limpa token de reset caso exista, por segurança
        u.setResetToken(null);
        u.setResetExpira(null);
        usuarioRepo.save(u);
    }

    // ── Helper ─────────────────────────────────────────────────────────────
    private UsuarioResumo toResumo(Usuario u, boolean adm) {
        UsuarioResumo r = new UsuarioResumo();
        r.setId(u.getId());
        r.setNome(u.getNome());
        r.setEmail(u.getEmail());
        r.setTelefone(u.getTelefone());
        r.setCurso(u.getCurso());
        r.setAdm(adm);
        return r;
    }
}
