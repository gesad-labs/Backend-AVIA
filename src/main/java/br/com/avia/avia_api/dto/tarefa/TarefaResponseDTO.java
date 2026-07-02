package br.com.avia.avia_api.dto.tarefa;

import br.com.avia.avia_api.enums.PrioridadeTarefa;
import br.com.avia.avia_api.enums.StatusTarefa;

public record TarefaResponseDTO(
        Long id,
        Long sprintId,
        String titulo,
        String descricao,
        Integer estimativa,
        StatusTarefa status,
        PrioridadeTarefa prioridade,
        Long tarefaParenteId,
        Boolean impedimento,
        Long responsavelId,
        String responsavelNome
) {}
