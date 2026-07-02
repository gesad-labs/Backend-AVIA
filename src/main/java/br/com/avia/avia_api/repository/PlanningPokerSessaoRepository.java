package br.com.avia.avia_api.repository;

import br.com.avia.avia_api.entity.PlanningPokerSessao;
import br.com.avia.avia_api.enums.StatusPlanningPokerSessao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface PlanningPokerSessaoRepository extends JpaRepository<PlanningPokerSessao, Long> {

    Optional<PlanningPokerSessao> findFirstByProjetoIdAndTarefaIdAndStatusInOrderByCriadoEmDesc(
            Long projetoId,
            Long tarefaId,
            Collection<StatusPlanningPokerSessao> status
    );
}