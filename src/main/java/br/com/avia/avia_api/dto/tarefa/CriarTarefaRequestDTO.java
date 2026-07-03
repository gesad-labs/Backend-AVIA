package br.com.avia.avia_api.dto.tarefa;

import br.com.avia.avia_api.enums.PrioridadeTarefa;
import br.com.avia.avia_api.enums.StatusTarefa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CriarTarefaRequestDTO(

        @NotBlank
        String titulo,

        @NotBlank
        String descricao,

        Integer estimativa,

        @NotNull
        StatusTarefa status,

        PrioridadeTarefa prioridade,

        Long sprintId,

        Long tarefaParenteId,

        Boolean impedimento,

        List<Long> responsaveisIds,

        String tipo
) {}
