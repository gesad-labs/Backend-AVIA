package br.com.avia.avia_api.dto.sprint;

import br.com.avia.avia_api.enums.StatusSprint;

public record SprintResponseDTO(
        Long id,
        String nome,
        Long projetoId,
        StatusSprint status
) {}

