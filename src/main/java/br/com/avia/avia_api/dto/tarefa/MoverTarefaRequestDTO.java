package br.com.avia.avia_api.dto.tarefa;

import jakarta.validation.constraints.NotNull;

public record MoverTarefaRequestDTO(
        @NotNull(message = "novaSprintId é obrigatório")
        Long novaSprintId
) {
}
