package br.com.avia.avia_api.dto.tarefa;

import br.com.avia.avia_api.enums.PrioridadeTarefa;

public record EditarTarefaRequestDTO(
        String titulo,
        String descricao,
        Integer estimativa,
        PrioridadeTarefa prioridade,
        Long responsavelId
) {}