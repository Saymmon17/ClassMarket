package com.classmarket.config;

import com.classmarket.model.Usuario;
import com.classmarket.repository.UsuarioRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer {

    private static final String ADM_EMAIL = "classmarket@proton.me";

    @Value("${ADM_SENHA}")
    private String admSenha;

    private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder   encoder;

    public DataInitializer(UsuarioRepository usuarioRepo, PasswordEncoder encoder) {
        this.usuarioRepo = usuarioRepo;
        this.encoder     = encoder;
    }

    @PostConstruct
    public void init() {
        try {
            // Tenta atualizar o ADM existente via JPQL direto (evita problema de mapeamento)
            int updated = usuarioRepo.atualizarSenhaEAtivo(
                    encoder.encode(admSenha), ADM_EMAIL);

            if (updated > 0) {
                System.out.println("[DataInitializer] ADM atualizado via JPQL.");
            } else {
                // Não existe — cria do zero
                Usuario adm = new Usuario();
                adm.setNome("Administrador");
                adm.setEmail(ADM_EMAIL);
                adm.setSenha(encoder.encode(admSenha));
                adm.setAtivo(true);
                adm.setCriadoEm(LocalDateTime.now());
                usuarioRepo.save(adm);
                System.out.println("[DataInitializer] ADM criado do zero.");
            }
        } catch (Exception e) {
            System.out.println("[DataInitializer] ERRO: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
