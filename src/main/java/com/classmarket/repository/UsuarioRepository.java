package com.classmarket.repository;

import com.classmarket.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByEmailAndAtivoTrue(String email);
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByResetToken(String token);
    List<Usuario> findAllByAtivoTrueOrderByNome();
    boolean existsByEmail(String email);

    @Modifying
    @Transactional
    @Query("UPDATE Usuario u SET u.senha = :senha, u.ativo = true WHERE u.email = :email")
    int atualizarSenhaEAtivo(@Param("senha") String senha, @Param("email") String email);
}
