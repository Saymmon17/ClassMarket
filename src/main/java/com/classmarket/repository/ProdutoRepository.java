package com.classmarket.repository;

import com.classmarket.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Integer> {

    // Todos os produtos ativos e aprovados
    List<Produto> findByAtivoTrueAndStatusOrderByNome(String status);

    // Filtrar por categoria
    @Query("SELECT p FROM Produto p WHERE p.ativo = true AND p.status = 'aprovado' " +
           "AND p.categoria.nome = :cat ORDER BY p.nome")
    List<Produto> findAprovadosByCategoria(@Param("cat") String cat);

    // Busca por nome ou descrição
    @Query("SELECT p FROM Produto p WHERE p.ativo = true AND p.status = 'aprovado' " +
           "AND (LOWER(p.nome) LIKE LOWER(CONCAT('%',:termo,'%')) " +
           "  OR LOWER(p.descricao) LIKE LOWER(CONCAT('%',:termo,'%'))) ORDER BY p.nome")
    List<Produto> searchAprovados(@Param("termo") String termo);

    // Produtos do vendedor (todos os status)
    List<Produto> findByVendedorIdAndAtivoTrueOrderByNome(Integer vendedorId);

    // Produtos pendentes (para o ADM)
    List<Produto> findByStatusAndAtivoTrueOrderByCriadoEmDesc(String status);

    // Todos (para o ADM)
    List<Produto> findAllByAtivoTrueOrderByCriadoEmDesc();
}
