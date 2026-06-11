package com.classmarket.service;

import com.classmarket.dto.Dto.*;
import com.classmarket.model.Avaliacao;
import com.classmarket.model.Notificacao;
import com.classmarket.model.Usuario;
import com.classmarket.repository.AvaliacaoRepository;
import com.classmarket.repository.NotificacaoRepository;
import com.classmarket.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class NotificacaoService {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final NotificacaoRepository notifRepo;
    private final AvaliacaoRepository   avaliacaoRepo;
    private final UsuarioRepository     usuarioRepo;

    public NotificacaoService(NotificacaoRepository notifRepo,
                               AvaliacaoRepository avaliacaoRepo,
                               UsuarioRepository usuarioRepo) {
        this.notifRepo     = notifRepo;
        this.avaliacaoRepo = avaliacaoRepo;
        this.usuarioRepo   = usuarioRepo;
    }

    // ── Deletar avaliação e notificar o usuário ────────────────────────────
    @Transactional
    public void deletarAvaliacaoComMotivo(Integer avaliacaoId, String motivo) {
        Avaliacao a = avaliacaoRepo.findById(avaliacaoId)
                .orElseThrow(() -> new RuntimeException("Avaliação não encontrada"));

        Usuario usuario = a.getUsuario();

        // Só notifica se a avaliação tiver um usuário vinculado
        if (usuario != null) {
            Notificacao n = new Notificacao();
            n.setUsuario(usuario);
            n.setTitulo("Sua avaliação foi removida");
            n.setMensagem("O administrador removeu sua avaliação" +
                    (a.getProduto() != null ? " sobre \"" + a.getProduto().getNome() + "\"" : "") +
                    ". Motivo: " + motivo);
            notifRepo.save(n);
        }

        avaliacaoRepo.delete(a);
    }

    // ── Buscar notificações do usuário logado ──────────────────────────────
    @Transactional(readOnly = true)
    public List<NotificacaoResponse> listarMinhas(String email) {
        Usuario u = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return notifRepo.findByUsuarioIdOrderByCriadoEmDesc(u.getId())
                .stream().map(this::toResponse).toList();
    }

    // ── Contar não lidas ───────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public long contarNaoLidas(String email) {
        Usuario u = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return notifRepo.countByUsuarioIdAndLidaFalse(u.getId());
    }

    // ── Marcar todas como lidas ────────────────────────────────────────────
    @Transactional
    public void marcarTodasComoLidas(String email) {
        Usuario u = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        notifRepo.marcarTodasComoLidas(u.getId());
    }

    // ── Mapper ─────────────────────────────────────────────────────────────
    private NotificacaoResponse toResponse(Notificacao n) {
        NotificacaoResponse r = new NotificacaoResponse();
        r.setId(n.getId());
        r.setTitulo(n.getTitulo());
        r.setMensagem(n.getMensagem());
        r.setLida(n.getLida());
        r.setCriadoEm(n.getCriadoEm().format(FMT));
        return r;
    }
}
