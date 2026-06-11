package com.classmarket.service;

import com.classmarket.dto.Dto.*;
import com.classmarket.model.Avaliacao;
import com.classmarket.model.Avaliacao.TipoAvaliacao;
import com.classmarket.model.Produto;
import com.classmarket.model.Usuario;
import com.classmarket.repository.AvaliacaoRepository;
import com.classmarket.repository.ProdutoRepository;
import com.classmarket.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AvaliacaoService {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final AvaliacaoRepository avaliacaoRepo;
    private final ProdutoRepository   produtoRepo;
    private final UsuarioRepository   usuarioRepo;

    public AvaliacaoService(AvaliacaoRepository avaliacaoRepo,
                            ProdutoRepository produtoRepo,
                            UsuarioRepository usuarioRepo) {
        this.avaliacaoRepo = avaliacaoRepo;
        this.produtoRepo   = produtoRepo;
        this.usuarioRepo   = usuarioRepo;
    }

    // ── Salvar avaliação ───────────────────────────────────────────────────
    @Transactional
    public AvaliacaoResponse salvar(AvaliacaoRequest req, String email) {
        Avaliacao a = new Avaliacao();
        a.setTipo(TipoAvaliacao.valueOf(req.getTipo()));
        a.setNota(req.getNota());
        a.setComentario(req.getComentario());

        if (email != null) {
            usuarioRepo.findByEmail(email).ifPresent(a::setUsuario);
        }

        if ("produto".equals(req.getTipo())) {
            if (req.getProdutoId() == null) {
                throw new RuntimeException("produtoId é obrigatório para avaliações de produto");
            }
            Produto p = produtoRepo.findById(req.getProdutoId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
            a.setProduto(p);
        }

        avaliacaoRepo.save(a);
        return toResponse(a);
    }

    // ── Listar todas ───────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<AvaliacaoResponse> listarTodas() {
        return avaliacaoRepo.findAllByOrderByCriadoEmDesc()
                .stream().map(this::toResponse).toList();
    }

    // ── Listar por produto ─────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<AvaliacaoResponse> listarPorProduto(Integer produtoId) {
        return avaliacaoRepo.findByProdutoIdOrderByCriadoEmDesc(produtoId)
                .stream().map(this::toResponse).toList();
    }

    // ── Mapper ────────────────────────────────────────────────────────────
    private AvaliacaoResponse toResponse(Avaliacao a) {
        AvaliacaoResponse r = new AvaliacaoResponse();
        r.setId(a.getId());
        r.setTipo(a.getTipo().name());
        r.setNota(a.getNota());
        r.setComentario(a.getComentario());
        r.setUsuarioNome(a.getUsuario() != null ? a.getUsuario().getNome() : "Anônimo");
        r.setProdutoNome(a.getProduto() != null ? a.getProduto().getNome() : null);
        r.setCriadoEm(a.getCriadoEm().format(FMT));
        return r;
    }
}
