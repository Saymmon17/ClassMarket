package com.classmarket.repository;

import com.classmarket.model.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Integer> {

    List<Avaliacao> findByProdutoIdOrderByCriadoEmDesc(Integer produtoId);

    List<Avaliacao> findAllByOrderByCriadoEmDesc();

    @Query("SELECT AVG(a.nota) FROM Avaliacao a WHERE a.produto.id = :produtoId")
    Optional<Double> mediaPorProduto(@Param("produtoId") Integer produtoId);
}
