package com.classmarket.config;

import com.classmarket.repository.UsuarioRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

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
            usuarioRepo.findByEmail(ADM_EMAIL).ifPresent(adm -> {
                adm.setSenha(encoder.encode(admSenha));
                adm.setAtivo(true);
                usuarioRepo.save(adm);
                System.out.println("[DataInitializer] ADM atualizado com sucesso.");
            });

            if (usuarioRepo.findByEmail(ADM_EMAIL).isEmpty()) {
                System.out.println("[DataInitializer] ATENCAO: ADM nao encontrado no banco.");
            }
        } catch (Exception e) {
            System.out.println("[DataInitializer] ERRO: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
