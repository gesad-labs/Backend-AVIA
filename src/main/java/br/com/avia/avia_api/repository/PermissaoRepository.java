package br.com.avia.avia_api.repository;

import br.com.avia.avia_api.entity.Permissao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissaoRepository extends JpaRepository<Permissao, Integer> {
    Permissao findByDescricao(String descricao);
}
