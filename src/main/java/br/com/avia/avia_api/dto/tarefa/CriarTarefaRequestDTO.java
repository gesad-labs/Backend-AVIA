package br.com.avia.avia_api.dto.tarefa;

import br.com.avia.avia_api.enums.PrioridadeTarefa;
import br.com.avia.avia_api.enums.StatusTarefa;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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

        Boolean impedimento
) {}