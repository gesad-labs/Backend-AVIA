package br.com.avia.avia_api.dto.sprint;

import br.com.avia.avia_api.enums.StatusSprint;

public record EditarSprintRequestDTO(
        String nome,
        StatusSprint status
) {
}