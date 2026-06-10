package com.classmarket.repository;

import com.classmarket.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByEmailAndAtivoTrue(String email);
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByResetToken(String token);
    List<Usuario> findAllByAtivoTrueOrderByNome();
    boolean existsByEmail(String email);
}
