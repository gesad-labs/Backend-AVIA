package br.com.avia.avia_api.dto.tarefa;

import jakarta.validation.constraints.NotBlank;

public record AtualizarStatusTarefaRequestDTO(

        @NotBlank(message = "O status é obrigatório")
        String status

) {}