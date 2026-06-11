package com.classmarket.service;

import com.classmarket.dto.Dto.*;
import com.classmarket.model.Categoria;
import com.classmarket.model.Produto;
import com.classmarket.model.Usuario;
import com.classmarket.repository.AvaliacaoRepository;
import com.classmarket.repository.CategoriaRepository;
import com.classmarket.repository.ProdutoRepository;
import com.classmarket.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository   produtoRepo;
    private final CategoriaRepository categoriaRepo;
    private final UsuarioRepository   usuarioRepo;
    private final AvaliacaoRepository avaliacaoRepo;

    public ProdutoService(ProdutoRepository produtoRepo,
                          CategoriaRepository categoriaRepo,
                          UsuarioRepository usuarioRepo,
                          AvaliacaoRepository avaliacaoRepo) {
        this.produtoRepo   = produtoRepo;
        this.categoriaRepo = categoriaRepo;
        this.usuarioRepo   = usuarioRepo;
        this.avaliacaoRepo = avaliacaoRepo;
    }

    // ── Listagem pública (aprovados) ───────────────────────────────────────
    @Transactional(readOnly = true)
    public List<ProdutoResponse> listarAprovados(String categoria, String busca) {
        List<Produto> lista;
        if (busca != null && !busca.isBlank()) {
            lista = produtoRepo.searchAprovados(busca);
        } else if (categoria != null && !categoria.isBlank()) {
            lista = produtoRepo.findAprovadosByCategoria(categoria);
        } else {
            lista = produtoRepo.findByAtivoTrueAndStatusOrderByNome("aprovado");
        }
        return lista.stream().map(this::toResponse).toList();
    }

    // ── Produto por ID (público) ───────────────────────────────────────────
    @Transactional(readOnly = true)
    public ProdutoResponse buscarPorId(Integer id) {
        Produto p = produtoRepo.findById(id)
                .filter(prod -> prod.getAtivo() && "aprovado".equals(prod.getStatus()))
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        return toResponse(p);
    }

    // ── Meus produtos ──────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<ProdutoResponse> meusProdutos(String email) {
        Usuario u = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return produtoRepo.findByVendedorIdAndAtivoTrueOrderByNome(u.getId())
                .stream().map(this::toResponse).toList();
    }

    // ── Cadastrar ──────────────────────────────────────────────────────────
    @Transactional
    public ProdutoResponse cadastrar(ProdutoRequest req, String emailVendedor) {
        Categoria cat = categoriaRepo.findById(req.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoria inválida"));
        Usuario vendedor = usuarioRepo.findByEmail(emailVendedor)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Produto p = new Produto();
        p.setNome(req.getNome());
        p.setDescricao(req.getDescricao());
        p.setPreco(req.getPreco());
        p.setEstoque(req.getEstoque());
        p.setCategoria(cat);
        p.setBloco(req.getBloco());
        p.setSala(req.getSala());
        p.setFotoUrl(req.getFotoUrl());
        p.setVendedor(vendedor);
        p.setStatus("pendente");
        produtoRepo.save(p);
        return toResponse(p);
    }

    // ── Editar (dono ou ADM) ───────────────────────────────────────────────
    @Transactional
    public ProdutoResponse atualizar(Integer id, ProdutoRequest req, String email, boolean adm) {
        Produto p = produtoRepo.findById(id)
                .filter(Produto::getAtivo)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        if (!adm && !p.getVendedor().getEmail().equals(email)) {
            throw new RuntimeException("Sem permissão");
        }

        Categoria cat = categoriaRepo.findById(req.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoria inválida"));

        p.setNome(req.getNome());
        p.setDescricao(req.getDescricao());
        p.setPreco(req.getPreco());
        p.setEstoque(req.getEstoque());
        p.setCategoria(cat);
        p.setBloco(req.getBloco());
        p.setSala(req.getSala());
        p.setFotoUrl(req.getFotoUrl());
        // Se o dono editar, volta para pendente; ADM mantém
        if (!adm) p.setStatus("pendente");
        produtoRepo.save(p);
        return toResponse(p);
    }

    // ── Excluir (dono ou ADM) ─────────────────────────────────────────────
    @Transactional
    public void excluir(Integer id, String email, boolean adm) {
        Produto p = produtoRepo.findById(id)
                .filter(Produto::getAtivo)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        if (!adm && !p.getVendedor().getEmail().equals(email)) {
            throw new RuntimeException("Sem permissão");
        }

        p.setAtivo(false);
        produtoRepo.save(p);
    }

    // ── ADM: listar todos / pendentes ──────────────────────────────────────
    @Transactional(readOnly = true)
    public List<ProdutoResponse> listarTodosAdm() {
        return produtoRepo.findAllByAtivoTrueOrderByCriadoEmDesc()
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listarPendentes() {
        return produtoRepo.findByStatusAndAtivoTrueOrderByCriadoEmDesc("pendente")
                .stream().map(this::toResponse).toList();
    }

    // ── ADM: aprovar / negar ───────────────────────────────────────────────
    @Transactional
    public ProdutoResponse mudarStatus(Integer id, String novoStatus) {
        if (!List.of("aprovado", "negado", "pendente").contains(novoStatus)) {
            throw new RuntimeException("Status inválido");
        }
        Produto p = produtoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        p.setStatus(novoStatus);
        produtoRepo.save(p);
        return toResponse(p);
    }

    // ── Mapper ────────────────────────────────────────────────────────────
    private ProdutoResponse toResponse(Produto p) {
        ProdutoResponse r = new ProdutoResponse();
        r.setId(p.getId());
        r.setNome(p.getNome());
        r.setDescricao(p.getDescricao());
        r.setPreco(p.getPreco());
        r.setEstoque(p.getEstoque());
        r.setCategoria(p.getCategoria().getNome());
        r.setCategoriaId(p.getCategoria().getId());
        r.setBloco(p.getBloco());
        r.setSala(p.getSala());
        r.setFotoUrl(p.getFotoUrl());
        r.setStatus(p.getStatus());

        VendedorResumo v = new VendedorResumo();
        v.setId(p.getVendedor().getId());
        v.setNome(p.getVendedor().getNome());
        v.setTelefone(p.getVendedor().getTelefone());
        r.setVendedor(v);

        double media = avaliacaoRepo.mediaPorProduto(p.getId()).orElse(0.0);
        long total   = avaliacaoRepo.findByProdutoIdOrderByCriadoEmDesc(p.getId()).size();
        r.setMediaAvaliacoes(Math.round(media * 10.0) / 10.0);
        r.setTotalAvaliacoes((int) total);

        return r;
    }
}
