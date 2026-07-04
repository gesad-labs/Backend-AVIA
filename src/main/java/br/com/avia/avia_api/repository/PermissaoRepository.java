package br.com.avia.avia_api.repository;

import br.com.avia.avia_api.entity.Permissao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissaoRepository extends JpaRepository<Permissao, Integer> {
    @Query("SELECT perm FROM Permissao perm WHERE perm.descricao = :descricao")
    Permissao findByDescricao(@Param("descricao") String descricao);
}
