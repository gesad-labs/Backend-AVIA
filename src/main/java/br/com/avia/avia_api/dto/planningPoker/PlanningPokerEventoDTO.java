package br.com.avia.avia_api.dto.planningPoker;

import br.com.avia.avia_api.enums.TipoEventoPlanningPoker;

public record PlanningPokerEventoDTO(
        TipoEventoPlanningPoker tipo,
        PlanningPokerSessaoResponseDTO sessao
) {
}