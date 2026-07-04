package br.com.avia.avia_api.dto.planningPoker;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record FinalizarPlanningPokerRequestDTO(
        @NotNull
        @PositiveOrZero
        Integer estimativaFinal
) {
}