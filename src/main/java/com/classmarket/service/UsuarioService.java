package com.classmarket.service;

import com.classmarket.dto.Dto.UsuarioResumo;
import com.classmarket.model.Usuario;
import com.classmarket.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepo;

    public UsuarioService(UsuarioRepository usuarioRepo) {
        this.usuarioRepo = usuarioRepo;
    }

    public List<UsuarioResumo> listar() {
        return usuarioRepo.findAllByAtivoTrueOrderByNome()
                .stream().map(this::toResumo).toList();
    }

    @Transactional
    public void desativar(Integer id) {
        Usuario u = usuarioRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        u.setAtivo(false);
        usuarioRepo.save(u);
    }

    private UsuarioResumo toResumo(Usuario u) {
        UsuarioResumo r = new UsuarioResumo();
        r.setId(u.getId());
        r.setNome(u.getNome());
        r.setEmail(u.getEmail());
        r.setTelefone(u.getTelefone());
        r.setCurso(u.getCurso());
        r.setAdm("classmarket@proton.me".equals(u.getEmail()));
        return r;
    }
}
