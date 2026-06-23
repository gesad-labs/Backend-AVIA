package br.com.avia.avia_api.dto.tarefa;

import jakarta.validation.constraints.NotBlank;

public record CriarTarefaRequestDTO(

        @NotBlank(message = "O título é obrigatório")
        String titulo,

        String descricao,

        int estimativa,

        @NotBlank(message = "A prioridade é obrigatória")
        String prioridade,

        Integer tarefaParenteId

) {}