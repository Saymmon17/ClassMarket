package com.classmarket.config;

import com.classmarket.model.Usuario;
import com.classmarket.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {

    private static final String ADM_EMAIL = "classmarket@proton.me";

    @Value("${ADM_SENHA}")
    private String admSenha;

    private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder   encoder;

    public DataInitializer(UsuarioRepository usuarioRepo, PasswordEncoder encoder) {
        this.usuarioRepo = usuarioRepo;
        this.encoder     = encoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        usuarioRepo.findByEmail(ADM_EMAIL).ifPresent(adm -> {
            if ("TROCAR_VIA_JAVA".equals(adm.getSenha())) {
                adm.setSenha(encoder.encode(admSenha));
                adm.setAtivo(true);
                usuarioRepo.save(adm);
                System.out.println("[DataInitializer] Senha do ADM definida com sucesso.");
            }
        });
    }
}
