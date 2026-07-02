package br.com.avia.avia_api.repository;

import br.com.avia.avia_api.entity.PlanningPokerVoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlanningPokerVotoRepository extends JpaRepository<PlanningPokerVoto, Long> {

    List<PlanningPokerVoto> findBySessaoIdAndRodada(Long sessaoId, Integer rodada);

    Optional<PlanningPokerVoto> findBySessaoIdAndUsuarioIdAndRodada(
            Long sessaoId,
            Long usuarioId,
            Integer rodada
    );
}