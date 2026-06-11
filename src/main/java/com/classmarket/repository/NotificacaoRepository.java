package com.classmarket.repository;

import com.classmarket.model.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Integer> {

    List<Notificacao> findByUsuarioIdOrderByCriadoEmDesc(Integer usuarioId);

    long countByUsuarioIdAndLidaFalse(Integer usuarioId);

    @Modifying
    @Transactional
    @Query("UPDATE Notificacao n SET n.lida = true WHERE n.usuario.id = :usuarioId")
    void marcarTodasComoLidas(@Param("usuarioId") Integer usuarioId);
}
