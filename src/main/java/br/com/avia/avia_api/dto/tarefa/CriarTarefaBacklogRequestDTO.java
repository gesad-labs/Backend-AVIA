package br.com.avia.avia_api.dto.tarefa;

import jakarta.validation.constraints.NotBlank;

public record CriarTarefaBacklogRequestDTO(

        @NotBlank(message = "O título é obrigatório")
        String titulo,

        String descricao,

        @NotBlank(message = "A prioridade é obrigatória")
        String prioridade,

        Long tarefaParenteId

) {}