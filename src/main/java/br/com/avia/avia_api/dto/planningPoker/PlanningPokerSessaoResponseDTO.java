package br.com.avia.avia_api.dto.planningPoker;

import br.com.avia.avia_api.enums.StatusPlanningPokerSessao;

import java.util.List;

public record PlanningPokerSessaoResponseDTO(
        Long id,
        Long projetoId,
        Long tarefaId,
        Long gerenteId,
        StatusPlanningPokerSessao status,
        Integer rodadaAtual,
        Integer estimativaFinal,
        List<ParticipantePlanningPokerResponseDTO> participantes
) {
}