package br.com.avia.avia_api.repository;

import br.com.avia.avia_api.entity.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, Long> {

    boolean existsByProjetoIdAndNome(Long projetoId, String nome);

    List<Sprint> findByProjetoId(Long projetoId);
}