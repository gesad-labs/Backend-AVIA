package br.com.avia.avia_api.dto.tarefa;

import jakarta.validation.constraints.NotBlank;

public record CriarTarefaRequestDTO(
        @NotBlank(message = "O título é obrigatório")
        String titulo,
        int estimativa,
        Integer tarefaParenteId
) {
}