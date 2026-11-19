package br.com.avia.avia_api.repository;

import br.com.avia.avia_api.entity.Sprint;
import br.com.avia.avia_api.enums.StatusSprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, Long> {

    boolean existsByProjetoIdAndNome(Long projetoId, String nome);

    List<Sprint> findByProjetoId(Long projetoId);

    boolean existsByProjetoIdAndStatus(Long projetoId, StatusSprint status);

    Optional<Sprint> findFirstByProjetoIdAndStatusOrderByIdAsc(Long projetoId, StatusSprint status);
}